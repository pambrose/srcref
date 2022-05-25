package com.pambrose.srcref

import com.github.pambrose.common.response.*
import com.pambrose.srcref.Page.displayForm
import com.pambrose.srcref.Page.urlPrefix
import com.pambrose.srcref.Urls.ARGS
import com.pambrose.srcref.Urls.EDIT
import com.pambrose.srcref.Urls.ERROR
import com.pambrose.srcref.Urls.GITHUB
import com.pambrose.srcref.Urls.MSG
import com.pambrose.srcref.Urls.githubRangeUrl
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
        deflate { priority = 10.0; minimumSize(1024) }
      }

      routing {
        get("/") {
          redirectTo { EDIT }
        }

        get(EDIT) {
          queryParams().also { params ->
            logger.info { params }
            displayForm(params)
          }
        }

        get(GITHUB) {
          queryParams().also { params ->
            logger.info { params }
            if (call.request.queryParameters.contains(EDIT))
              displayForm(params)
            else
              redirectTo { githubRangeUrl(params, urlPrefix) }
          }
        }

        get(ERROR) {
          val msgval = call.request.queryParameters[MSG] ?: "Missing message"
          val argsval = call.request.queryParameters[ARGS] ?: "Missing args"
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
