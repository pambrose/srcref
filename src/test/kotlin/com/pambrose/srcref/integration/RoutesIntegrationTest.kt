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

package com.pambrose.srcref.integration

import com.pambrose.srcref.ContentCache.CacheContent
import com.pambrose.srcref.ContentCache.Companion.contentCache
import com.pambrose.srcref.module
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication

class RoutesIntegrationTest :
  StringSpec(
    {
      "GET / redirects to /edit" {
        testApplication {
          application { module() }
          val noRedirectClient =
            createClient {
              followRedirects = false
            }
          noRedirectClient.get("/").apply {
            status shouldBe HttpStatusCode.Found
          }
        }
      }

      "GET /ping returns pong" {
        testApplication {
          application { module() }
          client.get("/ping").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldBe "pong"
          }
        }
      }

      "GET /edit returns form page" {
        testApplication {
          application { module() }
          client.get("/edit").apply {
            status shouldBe HttpStatusCode.OK
            val body = bodyAsText()
            body shouldContain "Username/Org Name:"
            body shouldContain "Example Values"
          }
        }
      }

      "GET /edit with partial params shows exception" {
        testApplication {
          application { module() }
          client.get("/edit?account=testuser").apply {
            status shouldBe HttpStatusCode.OK
            val body = bodyAsText()
            body shouldContain "Exception:"
            body shouldContain "Missing: repo value"
          }
        }
      }

      "GET /version returns version page" {
        testApplication {
          application { module() }
          client.get("/version").apply {
            status shouldBe HttpStatusCode.OK
            val body = bodyAsText()
            body shouldContain "Version"
            body shouldContain "Release Date"
          }
        }
      }

      "GET /what returns documentation page" {
        testApplication {
          application { module() }
          client.get("/what").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldContain "What is srcref?"
          }
        }
      }

      "GET /cache returns cache page" {
        testApplication {
          application { module() }
          client.get("/cache").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldContain "Cache Size"
          }
        }
      }

      "GET /cache renders populated entries with table" {
        val rawUrl = "https://raw.githubusercontent.com/u/r/main/file.kt"
        val shortEtagUrl = "https://example.com/short-etag-cache-test"
        val longEtagUrl = "https://example.com/long-etag-cache-test"
        val longEtag = "\"abcdefghijklmnopqrstuvwxyz1234567890\""
        try {
          contentCache[rawUrl] =
            CacheContent(pageLines = listOf("a", "b", "c"), etag = "\"short\"", contentLength = 12)
              .apply { markReferenced() }
          contentCache[shortEtagUrl] =
            CacheContent(pageLines = listOf("x"), etag = "\"e\"", contentLength = 1)
          contentCache[longEtagUrl] =
            CacheContent(pageLines = listOf("y"), etag = longEtag, contentLength = 1)

          testApplication {
            application { module() }
            client.get("/cache").apply {
              status shouldBe HttpStatusCode.OK
              val body = bodyAsText()
              body shouldContain "cachetable"
              // Raw GitHub prefix is stripped from the URL display column
              body shouldContain "/u/r/main/file.kt"
              body shouldNotContain "raw.githubusercontent.com"
              // Long etags get truncated with an ellipsis
              body shouldContain "..."
              // Both other test URLs render
              body shouldContain shortEtagUrl
              body shouldContain longEtagUrl
            }
          }
        } finally {
          contentCache.remove(rawUrl)
          contentCache.remove(shortEtagUrl)
          contentCache.remove(longEtagUrl)
        }
      }

      "GET /problem with msg displays error message" {
        testApplication {
          application { module() }
          client.get("/problem?msg=Test+Error+Message").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldContain "Test Error Message"
          }
        }
      }

      "GET /problem without msg shows missing message" {
        testApplication {
          application { module() }
          client.get("/problem").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldContain "Missing message value"
          }
        }
      }

      "GET /github?edit shows edit form" {
        testApplication {
          application { module() }
          client.get("/github?edit").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldContain "Username/Org Name:"
          }
        }
      }

      "GET /github without params redirects" {
        testApplication {
          application { module() }
          val noRedirectClient =
            createClient {
              followRedirects = false
            }
          noRedirectClient.get("/github").apply {
            status shouldBe HttpStatusCode.Found
          }
        }
      }

      "GET /robots.txt returns robot rules with correct paths" {
        testApplication {
          application { module() }
          client.get("/robots.txt").apply {
            status shouldBe HttpStatusCode.OK
            val body = bodyAsText()
            body shouldContain "User-agent"
            body shouldContain "Disallow: /problem"
            body shouldContain "Disallow: /cache"
            body shouldContain "Disallow: /threaddump"
            body shouldNotContain "/error"
          }
        }
      }

      "GET /threaddump returns content" {
        testApplication {
          application { module() }
          client.get("/threaddump").apply {
            status shouldBe HttpStatusCode.OK
          }
        }
      }

      "GET /nonexistent returns 404" {
        testApplication {
          application { module() }
          client.get("/nonexistent").apply {
            status shouldBe HttpStatusCode.NotFound
            bodyAsText() shouldContain "Page not found"
          }
        }
      }
    },
  )
