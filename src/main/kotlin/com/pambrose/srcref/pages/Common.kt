package com.pambrose.srcref.pages

import io.ktor.http.ContentType.Text.CSS
import kotlinx.html.*

object Common {
  internal const val WIDTH_VAL = "93"

  internal val URL_PREFIX = (System.getenv("PREFIX") ?: "http://localhost:8080").removeSuffix("/")

  internal fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }

  internal fun HEAD.commonHead() {
    meta { charset = "utf-8" }
    meta {
      name = "apple-mobile-web-app-capable"
      content = "yes"
    }
    meta {
      name = "apple-mobile-web-app-status-bar-style"
      content = "black-translucent"
    }
    meta {
      name = "viewport"
      content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
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
  }

  internal fun BODY.githubIcon() {
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
  }

  @DslMarker
  annotation class SrcRefDslTag

  internal fun Map<String, String?>.hasValues() = values.asSequence().filter { it?.isNotBlank() == true }.any()
}
