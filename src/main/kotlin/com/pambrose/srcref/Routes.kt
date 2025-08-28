package com.pambrose.srcref

import com.codahale.metrics.jvm.ThreadDump
import com.github.pambrose.common.response.redirectTo
import com.pambrose.srcref.Endpoints.CACHE
import com.pambrose.srcref.Endpoints.EDIT
import com.pambrose.srcref.Endpoints.GITHUB
import com.pambrose.srcref.Endpoints.PING
import com.pambrose.srcref.Endpoints.PROBLEM
import com.pambrose.srcref.Endpoints.THREADDUMP
import com.pambrose.srcref.Endpoints.VERSION
import com.pambrose.srcref.Endpoints.WHAT
import com.pambrose.srcref.Urls.MSG
import com.pambrose.srcref.Urls.githubRangeUrl
import com.pambrose.srcref.pages.Cache.displayCache
import com.pambrose.srcref.pages.Common.URL_PREFIX
import com.pambrose.srcref.pages.Edit.displayEdit
import com.pambrose.srcref.pages.Error.displayException
import com.pambrose.srcref.pages.Version.displayVersion
import com.pambrose.srcref.pages.What.displayWhat
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType.Text.Plain
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.ByteArrayOutputStream
import java.lang.management.ManagementFactory

object Routes {
  private val logger = KotlinLogging.logger {}

  fun Application.configureRoutes() {
    routing {
      // This will redirect to the www subdomain
      get("/") { redirectTo { "$URL_PREFIX/${EDIT.path}" } }

      get(EDIT.path) {
        val params = readQueryParams()
        logger.info { params }
        displayEdit(params)
      }

      get(GITHUB.path) {
        val params = readQueryParams()
        logger.info { params }
        if (call.request.queryParameters.contains(EDIT.path)) {
          displayEdit(params)
        } else {
          redirectTo { githubRangeUrl(params, URL_PREFIX).first }
        }
      }

      get(PROBLEM.path) {
        val params = readQueryParams()
        val msg = readMsg()
        displayException(params, msg)
      }

      get(WHAT.path) { displayWhat() }

      get(CACHE.path) { displayCache() }

      get(VERSION.path) { displayVersion() }

      get(PING.path) { call.respondText("pong", Plain) }

      get(THREADDUMP.path) {
        try {
          ByteArrayOutputStream()
            .apply {
              use { ThreadDumpInfo.threadDump.dump(true, true, it) }
            }.let { baos ->
              String(baos.toByteArray(), Charsets.UTF_8)
            }
        } catch (e: NoClassDefFoundError) {
          "Sorry, your runtime environment does not allow dump threads."
        }.also {
          call.respondText(it, Plain)
        }
      }

      get("robots.txt") {
        call.respondText(
          """
            User-agent: *
            Disallow: /error/
            Disallow: /error
          """.trimIndent(),
          Plain,
        )
      }

      staticResources("/", "public")
    }
  }

  private fun RoutingContext.readMsg() = call.request.queryParameters[MSG] ?: "Missing message value"

  private fun RoutingContext.readQueryParams() =
    buildMap {
      QueryParams.entries
        .map { it.arg }
        .forEach { arg -> put(arg, call.request.queryParameters[arg]) }
    }

  private object ThreadDumpInfo {
    val threadDump by lazy { ThreadDump(ManagementFactory.getThreadMXBean()) }
  }
}
