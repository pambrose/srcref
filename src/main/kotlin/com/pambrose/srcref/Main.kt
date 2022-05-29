package com.pambrose.srcref

import com.github.pambrose.common.response.*
import com.pambrose.srcref.Urls.CACHE
import com.pambrose.srcref.Urls.EDIT
import com.pambrose.srcref.Urls.ERROR
import com.pambrose.srcref.Urls.GITHUB
import com.pambrose.srcref.Urls.MSG
import com.pambrose.srcref.Urls.githubRangeUrl
import com.pambrose.srcref.pages.Cache.displayCache
import com.pambrose.srcref.pages.Common.urlPrefix
import com.pambrose.srcref.pages.Error.displayError
import com.pambrose.srcref.pages.Form.displayForm
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
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

      routing {
        get("/") {
          redirectTo { EDIT }
        }

        get(EDIT) {
          val params = readQueryParams()
          logger.info { params }
          displayForm(params)
        }

        get(GITHUB) {
          val params = readQueryParams()
          logger.info { params }
          if (call.request.queryParameters.contains(EDIT))
            displayForm(params)
          else
            redirectTo { githubRangeUrl(params, urlPrefix).first }
        }

        get(ERROR) {
          val params = readQueryParams()
          val msg = readMsg()
          displayError(params, msg)
        }

        get(CACHE) {
          displayCache()
        }

        static("/") {
          staticBasePackage = "public"
          resources(".")
        }
      }
    }.start(wait = true)
  }
}

private fun PipelineCall.readQueryParams() =
  buildMap {
    QueryArgs
      .values()
      .map { it.arg }
      .forEach { arg -> put(arg, call.request.queryParameters[arg]) }
  }

private fun PipelineCall.readMsg() = call.request.queryParameters[MSG] ?: "Missing message"

typealias PipelineCall = PipelineContext<Unit, ApplicationCall>