package com.pambrose.srcref

import com.github.pambrose.common.response.*
import com.pambrose.srcref.Urls.CACHE
import com.pambrose.srcref.Urls.EDIT
import com.pambrose.srcref.Urls.ERROR
import com.pambrose.srcref.Urls.GITHUB
import com.pambrose.srcref.Urls.MSG
import com.pambrose.srcref.Urls.VERSION
import com.pambrose.srcref.Urls.githubRangeUrl
import com.pambrose.srcref.pages.Cache.displayCache
import com.pambrose.srcref.pages.Common.urlPrefix
import com.pambrose.srcref.pages.Error.displayError
import com.pambrose.srcref.pages.Form.displayForm
import com.pambrose.srcref.pages.Version.displayVersion
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import mu.*

object Routes : KLogging() {
  fun Application.routes() {
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

      get(VERSION) {
        displayVersion()
      }

      static("/") {
        staticBasePackage = "public"
        resources(".")
      }
    }
  }

  private fun PipelineCall.readMsg() = call.request.queryParameters[MSG] ?: "Missing message value"

  private fun PipelineCall.readQueryParams() =
    buildMap {
      QueryArgs
        .values()
        .map { it.arg }
        .forEach { arg -> put(arg, call.request.queryParameters[arg]) }
    }
}