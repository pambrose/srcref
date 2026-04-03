package com.pambrose.srcref.pages

import com.pambrose.srcref.Endpoints.EDIT
import com.pambrose.srcref.Urls.toQueryParams
import com.pambrose.srcref.pages.Common.WIDTH_VAL
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.routing.RoutingContext
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.onClick
import kotlinx.html.p
import kotlinx.html.textArea

/** Error page displayed when srcref URL resolution fails. */
object Error {
  /**
   * Renders an error page showing the exception [msg] and the original [params],
   * with a button to return to the edit form with the same parameter values.
   */
  internal suspend fun RoutingContext.displayException(
    params: Map<String, String?>,
    msg: String,
  ) {
    call.respondHtmlTemplate(PageTemplate("srcref Exception")) {
      content {
        div("page-indent") {
          h2 { +"srcref Exception:" }
          textArea {
            rows = "3"
            cols = WIDTH_VAL
            readonly = true
            +msg
          }
          h2 { +"Args:" }
          textArea {
            rows = "5"
            cols = WIDTH_VAL
            readonly = true
            +params.toString()
          }
          p {
            button(classes = "button") {
              onClick = "window.open('/$EDIT?${params.toQueryParams(false)}', '_self')"
              +"Edit Values"
            }
          }
        }
      }
    }
  }
}
