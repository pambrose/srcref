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
