package com.pambrose.srcref.pages

import com.github.pambrose.srcref.srcref.BuildConfig.RELEASE_DATE
import com.github.pambrose.srcref.srcref.BuildConfig.VERSION
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.routing.RoutingContext
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.p

object Version {
  internal suspend fun RoutingContext.displayVersion() {
    call.respondHtmlTemplate(PageTemplate("srcref Version")) {
      content {
        div("page-indent") {
          h2 { +"srcref Version" }
          p { +"Version: $VERSION" }
          p { +"Release Date: $RELEASE_DATE" }
        }
      }
    }
  }
}
