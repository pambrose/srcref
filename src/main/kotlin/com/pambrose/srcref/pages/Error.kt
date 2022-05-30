package com.pambrose.srcref.pages

import com.github.pambrose.common.response.*
import com.pambrose.srcref.*
import com.pambrose.srcref.Endpoints.EDIT
import com.pambrose.srcref.Urls.toQueryParams
import com.pambrose.srcref.pages.Common.commonHead
import com.pambrose.srcref.pages.Common.githubIcon
import com.pambrose.srcref.pages.Common.widthVal
import kotlinx.html.*
import kotlinx.html.dom.*

object Error {
  internal suspend fun PipelineCall.displayError(params: Map<String, String?>, msg: String) {
    respondWith {
      document {
        append.html {
          head {
            commonHead()
            title { +"srcref Error" }
          }
          body {
            githubIcon()
            div {
              style = "padding-left: 20px;"
              h2 { +"srcref Exception:" }
              textArea { rows = "3"; cols = widthVal; readonly = true; +msg }
              h2 { +"Args:" }
              textArea { rows = "5"; cols = widthVal; readonly = true; +params.toString() }
              div {
                style = "padding-top: 20px;"
                button(classes = "button") {
                  onClick = "window.open('/$EDIT?${params.toQueryParams(false)}', '_self')"
                  +"Edit Values"
                }
              }
            }
          }
        }
      }.serialize()
    }
  }
}