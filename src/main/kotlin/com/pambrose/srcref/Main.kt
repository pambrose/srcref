package com.pambrose.srcref

import com.github.pambrose.common.util.*
import com.github.pambrose.common.util.Version.Companion.versionDesc
import com.github.pambrose.srcref.srcref.*
import com.pambrose.srcref.Endpoints.PING
import com.pambrose.srcref.Routes.routes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import mu.*
import org.slf4j.event.*

@Version(version = BuildConfig.VERSION, date = BuildConfig.RELEASE_DATE)
object SrcRef : KLogging() {
  @JvmStatic
  fun main(args: Array<String>) {
    logger.apply {
      info { getBanner("banners/srcref.txt", this) }
      info { SrcRef::class.versionDesc() }
    }

    embeddedServer(CIO, port = System.getenv("PORT")?.toInt() ?: 8080) {
      install(CallLogging) {
        level = Level.INFO
        // Do not log ping calls
        filter { call -> !call.request.path().startsWith("/${PING.path}") }
      }
      install(DefaultHeaders) { header("X-Engine", "Ktor") }
      install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
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

internal typealias PipelineCall = PipelineContext<Unit, ApplicationCall>