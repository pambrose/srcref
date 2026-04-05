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

import com.pambrose.srcref.ContentCache.Companion.contentCache
import com.pambrose.srcref.Urls.RAW_PREFIX
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.routing.RoutingContext
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.id
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr

/** Cache status page showing all cached file entries with their access statistics. */
object Cache {
  /** Renders a table of all cached entries sorted by last access time. */
  internal suspend fun RoutingContext.displayCache() {
    call.respondHtmlTemplate(PageTemplate("srcref Content Cache")) {
      content {
        div("page-indent") {
          h2 { +"srcref Content Cache" }
          h3 { +"Cache Size: ${contentCache.size}" }
          if (contentCache.size > 0) {
            table {
              id = "cachetable"
              style = "width: 100%; border-collapse: collapse;"
              tr {
                th { +"Hits" }
                th { +"Last Access" }
                th { +"Age" }
                th { +"Size" }
                th { +"Lines" }
                th {
                  style = "padding-left: 5px; text-align: left;"
                  +"Url"
                }
                th {
                  style = "padding-left: 5px; text-align: left;"
                  +"ETag"
                }
              }
              contentCache.sortedByLastReferenced().forEach { (url, v) ->
                tr {
                  td {
                    style = "text-align: center;"
                    +v.hits.toString()
                  }
                  td {
                    style = "text-align: center;"
                    +v.lastReferenced.toString()
                  }
                  td {
                    style = "text-align: center;"
                    +v.age.toString()
                  }
                  td {
                    style = "text-align: center;"
                    +v.contentLength.toString()
                  }
                  td {
                    style = "text-align: center;"
                    +v.pageLines.size.toString()
                  }
                  td { +url.let { if (url.startsWith(RAW_PREFIX)) it.substring(RAW_PREFIX.length) else it } }
                  td {
                    +v.etag
                      .substring(1..minOf(20, v.etag.length - 1))
                      .let { if (v.etag.length > 21) "$it..." else it }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
