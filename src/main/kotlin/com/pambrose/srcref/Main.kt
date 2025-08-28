package com.pambrose.srcref

import com.github.pambrose.common.util.Version
import com.github.pambrose.common.util.Version.Companion.versionDesc
import com.github.pambrose.common.util.getBanner
import com.github.pambrose.srcref.srcref.BuildConfig
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
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import org.slf4j.event.Level

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

fun String.startsWithList(prefixes: Iterable<String>) = prefixes.any { startsWith(it) }

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
      val path = call.request.path()
      val userAgent = call.request.headers["User-Agent"]
      "[$userAgent] $path"
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
