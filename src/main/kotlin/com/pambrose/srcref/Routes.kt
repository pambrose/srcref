package com.pambrose.srcref

import com.github.pambrose.common.response.PipelineCall
import com.github.pambrose.common.response.redirectTo
import com.pambrose.srcref.Endpoints.*
import com.pambrose.srcref.Urls.MSG
import com.pambrose.srcref.Urls.githubRangeUrl
import com.pambrose.srcref.pages.Cache.displayCache
import com.pambrose.srcref.pages.Common.URL_PREFIX
import com.pambrose.srcref.pages.Edit.displayEdit
import com.pambrose.srcref.pages.Error.displayException
import com.pambrose.srcref.pages.Version.displayVersion
import com.pambrose.srcref.pages.What.displayWhat
import io.ktor.http.ContentType.Text.Plain
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.two.KLogging

object Routes : KLogging() {
    fun Application.routes() {
        routing {
            // This will redirect to www subdomain
            get("/") { redirectTo { "$URL_PREFIX/${EDIT.path}" } }

            get(EDIT.path) {
                val params = readQueryParams()
                logger.info { params }
                displayEdit(params)
            }

            get(GITHUB.path) {
                val params = readQueryParams()
                logger.info { params }
                if (call.request.queryParameters.contains(EDIT.path))
                    displayEdit(params)
                else
                    redirectTo { githubRangeUrl(params, URL_PREFIX).first }
            }

            get(ERROR.path) {
                val params = readQueryParams()
                val msg = readMsg()
                displayException(params, msg)
            }

            get(WHAT.path) { displayWhat() }

            get(CACHE.path) { displayCache() }

            get(VERSION.path) { displayVersion() }

            get(PING.path) { call.respondText("pong", Plain) }

            get("robots.txt") {
                call.respondText(
                    """
            User-agent: *
            Disallow: /error/
            Disallow: /error
          """.trimIndent(),
                    Plain
                )
            }

            staticResources("/", "public")
        }
    }

    private fun PipelineCall.readMsg() = call.request.queryParameters[MSG] ?: "Missing message value"

    private fun PipelineCall.readQueryParams() =
        buildMap {
            QueryParams.entries
                .map { it.arg }
                .forEach { arg -> put(arg, call.request.queryParameters[arg]) }
        }
}