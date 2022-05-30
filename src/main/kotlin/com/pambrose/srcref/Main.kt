package com.pambrose.srcref

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

object SrcRef : KLogging() {
  @JvmStatic
  fun main(args: Array<String>) {
    embeddedServer(CIO, port = System.getenv("PORT")?.toInt() ?: 8080) {
      install(CallLogging)
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