/*
 *   Copyright © 2026 Paul Ambrose (pambrose@mac.com)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.pambrose.srcref.core

import com.pambrose.srcref.ContentCache
import com.pambrose.srcref.ContentCache.Companion.contentCache
import com.pambrose.srcref.ContentCache.Companion.fetchContent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.header
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.runBlocking

/**
 * Exercises [ContentCache.Companion.fetchContent] against a real local HTTP server,
 * since the underlying client is hard-coded inside the companion and cannot be injected.
 */
class FetchContentTest :
  StringSpec(
    {
      lateinit var server: EmbeddedServer<*, *>
      var port = 0

      val etag = AtomicReference("")
      val contentType = AtomicReference("text/plain; charset=UTF-8")
      val body = AtomicReference("alpha\nbeta\ngamma")
      val sendOversizedBody = AtomicReference(false)

      beforeSpec {
        server =
          embeddedServer(CIO, port = 0) {
            routing {
              get("/file") {
                etag.get().takeIf { it.isNotEmpty() }?.let {
                  call.response.header(HttpHeaders.ETag, it)
                  if (call.request.headers[HttpHeaders.IfNoneMatch] == it) {
                    call.respondBytes(ByteArray(0), status = HttpStatusCode.NotModified)
                    return@get
                  }
                }
                val ct = ContentType.parse(contentType.get())
                if (sendOversizedBody.get()) {
                  // Allocate 6 MB so Content-Length comfortably exceeds the 5 MB max-length cap.
                  call.respondBytes(ByteArray(6 * 1_048_576) { 'a'.code.toByte() }, ct)
                } else {
                  call.respondText(body.get(), ct)
                }
              }
            }
          }
        server.start(wait = false)
        port = runBlocking { server.engine.resolvedConnectors().first().port }
      }

      afterSpec {
        server.stop(0, 0)
      }

      fun reset(
        etagVal: String = "",
        contentTypeVal: String = "text/plain; charset=UTF-8",
        bodyVal: String = "alpha\nbeta\ngamma",
        oversized: Boolean = false,
      ) {
        etag.set(etagVal)
        contentType.set(contentTypeVal)
        body.set(bodyVal)
        sendOversizedBody.set(oversized)
      }

      "fetchContent caches by ETag and returns lines" {
        reset(etagVal = "\"v1\"")
        val url = "http://localhost:$port/file?cache-etag"
        contentCache.remove(url)
        val lines = fetchContent(url)
        lines shouldBe listOf("alpha", "beta", "gamma")
        val cached = contentCache[url]
        cached shouldNotBe null
        cached!!.etag shouldBe "\"v1\""
        cached.pageLines shouldBe listOf("alpha", "beta", "gamma")
      }

      "fetchContent without ETag does not populate cache" {
        reset(etagVal = "")
        val url = "http://localhost:$port/file?no-etag"
        contentCache.remove(url)
        val lines = fetchContent(url)
        lines shouldBe listOf("alpha", "beta", "gamma")
        contentCache[url] shouldBe null
      }

      "fetchContent returns cached value on 304" {
        reset(etagVal = "\"v304\"")
        val url = "http://localhost:$port/file?cond-get"
        contentCache.remove(url)

        fetchContent(url)
        val firstHits = contentCache[url]!!.hits

        val second = fetchContent(url)
        second shouldBe listOf("alpha", "beta", "gamma")
        contentCache[url]!!.hits shouldBe firstHits + 1
      }

      "fetchContent rejects binary content type (image/png)" {
        reset(contentTypeVal = "image/png")
        val url = "http://localhost:$port/file?image"
        contentCache.remove(url)
        shouldThrow<IllegalArgumentException> {
          fetchContent(url)
        }.message shouldContain "Invalid content type"
      }

      "fetchContent rejects application/* content type" {
        reset(contentTypeVal = "application/octet-stream")
        val url = "http://localhost:$port/file?app"
        contentCache.remove(url)
        shouldThrow<IllegalArgumentException> {
          fetchContent(url)
        }.message shouldContain "Invalid content type"
      }

      "fetchContent accepts non-text content type with warning" {
        // "weird/thing" is neither invalid nor text/* — should warn but proceed.
        reset(contentTypeVal = "weird/thing")
        val url = "http://localhost:$port/file?weird"
        contentCache.remove(url)
        val lines = fetchContent(url)
        lines shouldBe listOf("alpha", "beta", "gamma")
      }

      "fetchContent throws when Content-Length exceeds maximum" {
        reset(oversized = true)
        val url = "http://localhost:$port/file?too-large"
        contentCache.remove(url)
        shouldThrow<IllegalArgumentException> {
          fetchContent(url)
        }.message shouldContain "exceeds maximum length"
      }
    },
  )
