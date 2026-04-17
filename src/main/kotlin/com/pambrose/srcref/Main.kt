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

package com.pambrose.srcref

import com.pambrose.common.util.Version
import com.pambrose.common.util.Version.Companion.versionDesc
import com.pambrose.common.util.getBanner
import com.pambrose.srcref.Endpoints.PING
import com.pambrose.srcref.Main.excludedEndpoints
import com.pambrose.srcref.Routes.configureRoutes
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.calllogging.processingTimeMillis
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import org.slf4j.event.Level

/**
 * Application entry point for the srcref web service.
 *
 * Starts a Ktor CIO embedded server, installs middleware (logging, headers, status pages,
 * compression), and delegates to [Routes.configureRoutes] for endpoint registration.
 */
@Version(
  version = BuildConfig.VERSION,
  releaseDate = BuildConfig.RELEASE_DATE,
  buildTime = BuildConfig.BUILD_TIME,
)
object Main {
  internal val logger = KotlinLogging.logger {}
  internal val excludedEndpoints = listOf("/${PING.path}")

  @JvmStatic
  fun main(args: Array<String>) {
    logger.apply {
      info { getBanner("banners/srcref.banner", this) }
      info { Main::class.versionDesc() }
    }

    embeddedServer(
      factory = CIO,
      port = System.getenv("PORT")?.toInt() ?: 8080,
      module = Application::module,
    ).start(wait = true)
  }
}

/** Returns `true` if this string starts with any of the given [prefixes]. */
fun String.startsWithList(prefixes: Iterable<String>) = prefixes.any { startsWith(it) }

/**
 * Ktor application module that installs middleware and configures routes.
 *
 * Middleware installed:
 * - [CallLogging] — request logging (excludes health checks and common bot probes).
 * - [DefaultHeaders] — standard HTTP response headers.
 * - [StatusPages] — custom 404 handling.
 * - [Compression] — gzip and deflate response compression.
 */
fun Application.module() {
  install(CallLogging) {
    level = Level.INFO
    filter { call ->
      call.request.path().run {
        !startsWithList(excludedEndpoints) &&
          !endsWith(".php") &&
          !endsWith("error.log") &&
          !endsWith("error.txt")
      }
    }
    format { call ->
      val status = call.response.status()
      val method = call.request.httpMethod.value
      val path = call.request.path()
      val duration = call.processingTimeMillis()
      val remote = call.request.origin.remoteHost
      "$status $method $path ${duration}ms from $remote"
    }
  }
  install(DefaultHeaders)
  install(StatusPages) {
    status(NotFound) { call, status ->
      val filename = call.request.path()
      val msg = "Page not found: $filename"
      call.respondText(text = msg, status = status)
    }
  }
  install(Compression) {
    gzip { priority = 1.0 }
    deflate {
      priority = 10.0
      minimumSize(1024)
    }
  }
  configureRoutes()
}
