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

import com.pambrose.srcref.pages.Common.rawHtml
import io.ktor.http.ContentType.Text.CSS
import io.ktor.server.html.Placeholder
import io.ktor.server.html.Template
import io.ktor.server.html.insert
import kotlinx.html.BODY
import kotlinx.html.HEAD
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.title

/**
 * Base HTML page template for all srcref pages.
 *
 * Provides a consistent layout with viewport meta tags, favicon, CSS, a GitHub source link
 * in the top-right corner, and placeholders for page-specific [script] and [content].
 *
 * @param titleTxt the HTML `<title>` text for the page.
 */
class PageTemplate(
  val titleTxt: String,
) : Template<HTML> {
  val script = Placeholder<HEAD>()
  val content = Placeholder<BODY>()

  override fun HTML.apply() {
    head {
      meta { charset = "utf-8" }
      meta {
        name = "apple-mobile-web-app-capable"
        this.content = "yes"
      }
      meta {
        name = "apple-mobile-web-app-status-bar-style"
        this.content = "black-translucent"
      }
      meta {
        name = "viewport"
        this.content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
      }
      link {
        rel = "shortcut icon"
        href = "/favicon.ico"
        type = "image/x-icon"
      }
      link {
        rel = "icon"
        href = "/favicon.ico"
        type = "image/x-icon"
      }
      link {
        rel = "stylesheet"
        href = "css/srcref.css"
        type = CSS.toString()
      }

      insert(script)
      title { +titleTxt }
    }
    body {
      a(href = "https://github.com/pambrose/srcref", target = "_blank", classes = "top-right") {
        title = "View source on GitHub"
        rawHtml(
          """
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 55 55">
            <path fill="currentColor" stroke="none" d="M27.5 11.2a16.3 16.3 0 0 0-5.1 31.7c.8.2 1.1-.3 1.1-.7v-2.8c-4.5 1-5.5-2.2-5.5-2.2-.7-1.9-1.8-2.4-1.8-2.4-1.5-1 .1-1 .1-1 1.6.1 2.5 1.7 2.5 1.7 1.5 2.5 3.8 1.8 4.7 1.4.2-1 .6-1.8 1-2.2-3.5-.4-7.3-1.8-7.3-8 0-1.8.6-3.3 1.6-4.4-.1-.5-.7-2.1.2-4.4 0 0 1.4-.4 4.5 1.7a15.6 15.6 0 0 1 8.1 0c3.1-2 4.5-1.7 4.5-1.7.9 2.3.3 4 .2 4.4 1 1 1.6 2.6 1.6 4.3 0 6.3-3.8 7.7-7.4 8 .6.6 1.1 1.6 1.1 3v4.6c0 .4.3.9 1.1.7a16.3 16.3 0 0 0-5.2-31.7"></path>
          </svg>
          """,
        )
      }

      insert(content)
    }
  }
}
