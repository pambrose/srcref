package com.pambrose.srcref.pages

import com.pambrose.srcref.BuildConfig.RELEASE_DATE
import com.pambrose.srcref.BuildConfig.VERSION
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.routing.RoutingContext
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.p

/** Version information page showing the current srcref version and release date. */
object Version {
  /** Renders the version page with build metadata from [BuildConfig]. */
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
