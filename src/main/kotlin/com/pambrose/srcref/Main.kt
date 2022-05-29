package com.pambrose.srcref

import com.pambrose.srcref.Routes.routes
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.util.pipeline.*
import mu.*

object SrcRef : KLogging() {
  @JvmStatic
  fun main(args: Array<String>) {
    embeddedServer(CIO, port = System.getenv("PORT")?.toInt() ?: 8080) {
      install(CallLogging)
      install(DefaultHeaders) { header("X-Engine", "Ktor") }
      install(Compression) {
        gzip { priority = 1.0 }
        deflate { priority = 10.0; minimumSize(1024) }
      }

      routes()
    }.start(wait = true)
  }
}

typealias PipelineCall = PipelineContext<Unit, ApplicationCall>