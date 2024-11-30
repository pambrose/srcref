package com.pambrose.srcref.pages

import com.github.pambrose.common.response.respondWith
import com.github.pambrose.srcref.srcref.BuildConfig.RELEASE_DATE
import com.github.pambrose.srcref.srcref.BuildConfig.VERSION
import com.pambrose.srcref.pages.Common.commonHead
import com.pambrose.srcref.pages.Common.githubIcon
import io.ktor.server.routing.RoutingContext
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.title

object Version {
  internal suspend fun RoutingContext.displayVersion() {
    respondWith {
      document {
        append.html {
          head {
            commonHead()
            title { +"srcref Version" }
          }
          body {
            githubIcon()
            div("page-indent") {
              h2 { +"srcref Version" }
              p { +"Version: $VERSION" }
              p { +"Release Date: $RELEASE_DATE" }
            }
          }
        }
      }.serialize()
    }
  }
}
