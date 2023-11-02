package com.pambrose.srcref.pages

import com.github.pambrose.common.response.PipelineCall
import com.github.pambrose.common.response.respondWith
import com.pambrose.srcref.Endpoints.EDIT
import com.pambrose.srcref.Urls.toQueryParams
import com.pambrose.srcref.pages.Common.WIDTH_VAL
import com.pambrose.srcref.pages.Common.commonHead
import com.pambrose.srcref.pages.Common.githubIcon
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize

object Error {
  internal suspend fun PipelineCall.displayException(
    params: Map<String, String?>,
    msg: String,
  ) {
    respondWith {
      document {
        append.html {
          head {
            commonHead()
            title { +"srcref Exception" }
          }
          body {
            githubIcon()
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
      }.serialize()
    }
  }
}
