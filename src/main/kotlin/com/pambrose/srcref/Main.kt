package com.pambrose.srcref

import com.github.pambrose.common.response.*
import com.pambrose.srcref.Page.displayForm
import com.pambrose.srcref.Page.urlPrefix
import com.pambrose.srcref.Utils.editRef
import com.pambrose.srcref.Utils.errorRef
import com.pambrose.srcref.Utils.githubRef
import com.pambrose.srcref.Utils.githubRefUrl
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import kotlinx.html.dom.*
import mu.*
import kotlin.collections.set

object SrcRef : KLogging() {
  @JvmStatic
  fun main(args: Array<String>) {
    embeddedServer(CIO, port = System.getenv("PORT")?.toInt() ?: 8080) {
      install(CallLogging)
      install(DefaultHeaders) { header("X-Engine", "Ktor") }
      install(Compression) {
        gzip { priority = 1.0 }
        deflate { priority = 10.0; minimumSize(1024) /* condition*/ }
      }

      routing {
        get("/") {
          redirectTo { "/$editRef" }
        }

        get(editRef) {
          queryParams().also { params ->
            logger.info { params }
            displayForm(params)
          }
        }

        get(githubRef) {
          queryParams().also { params ->
            logger.info { params }
            if (call.request.queryParameters.contains("edit"))
              displayForm(params)
            else
              redirectTo { githubRefUrl(params, urlPrefix) }
          }
        }

        get(errorRef) {
          val msgval = call.request.queryParameters["msg"] ?: "Missing message"
          val argsval = call.request.queryParameters["args"] ?: "Missing args"
          respondWith {
            document {
              append.html {
                head {}
                body {
                  h1 { +"Srcref Exception" }
                  h2 { +msgval }
                  h2 { +"Args" }
                  div {
                    style = "padding-left: 20px;"
                    textArea {
                      rows = "5"
                      cols = "100"
                      readonly = true
                      +argsval
                    }
                  }
                }
              }
            }.serialize()
          }
        }

        static("/") {
          staticBasePackage = "public"
          resources(".")
        }
      }
    }.start(wait = true)
  }
}

fun PipelineCall.queryParams() =
  mutableMapOf<String, String?>()
    .also {
      QueryArgs
        .values()
        .map { it.arg }
        .forEach { arg -> it[arg] = call.request.queryParameters[arg] }
    }

typealias PipelineCall = PipelineContext<Unit, ApplicationCall>
