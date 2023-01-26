package com.pambrose.srcref

import com.github.pambrose.common.util.Version
import com.github.pambrose.common.util.Version.Companion.versionDesc
import com.github.pambrose.common.util.getBanner
import com.github.pambrose.srcref.srcref.BuildConfig
import com.pambrose.srcref.Endpoints.PING
import com.pambrose.srcref.Routes.routes
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import mu.KLogging
import org.slf4j.event.Level

@Version(version = BuildConfig.VERSION, date = BuildConfig.RELEASE_DATE)
object Main : KLogging() {
  @JvmStatic
  fun main(args: Array<String>) {
    logger.apply {
      info { getBanner("banners/srcref.banner", this) }
      info { Main::class.versionDesc() }
    }

    embeddedServer(CIO, port = System.getenv("PORT")?.toInt() ?: 8080) {
      install(CallLogging) {
        level = Level.INFO
        filter { call -> !call.request.path().startsWith("/${PING.path}") }
        format { call ->
          val path = call.request.path()
          val userAgent = call.request.headers["User-Agent"]
          "[$userAgent] $path"
        }
      }
      install(DefaultHeaders)
      install(StatusPages) {
        status(NotFound) { call, status ->
          val msg = "Page not found: ${call.request.path()}"
          call.respondText(text = msg, status = status)
          logger.info { msg }
        }
      }
      install(Compression) {
        gzip { priority = 1.0 }
        deflate { priority = 10.0; minimumSize(1024) }
      }
      routes()
    }.start(wait = true)
  }
}