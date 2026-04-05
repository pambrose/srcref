/*
 *   Copyright © 2026 Paul Ambrose (pambrose@mac.com)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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
