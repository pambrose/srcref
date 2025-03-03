package com.pambrose.srcref

import com.github.pambrose.common.util.Version
import com.github.pambrose.common.util.Version.Companion.versionDesc
import com.github.pambrose.common.util.getBanner
import com.github.pambrose.srcref.srcref.BuildConfig
import com.pambrose.srcref.Endpoints.PING
import com.pambrose.srcref.Main.excludedEndpoints
import com.pambrose.srcref.Routes.routes
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
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
      call.request.path().run { !startsWithList(excludedEndpoints) && !endsWith(".php") }
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
  routes()
}
