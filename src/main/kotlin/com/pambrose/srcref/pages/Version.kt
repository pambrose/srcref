package com.pambrose.srcref.pages

import com.github.pambrose.common.response.PipelineCall
import com.github.pambrose.common.response.respondWith
import com.github.pambrose.srcref.srcref.BuildConfig.RELEASE_DATE
import com.github.pambrose.srcref.srcref.BuildConfig.VERSION
import com.pambrose.srcref.pages.Common.commonHead
import com.pambrose.srcref.pages.Common.githubIcon
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize

object Version {
  internal suspend fun PipelineCall.displayVersion() {
    respondWith {
      document {
        append.html {
          head {
            commonHead()
            title { +"srcref Version" }
          }
          body {
            githubIcon()
            div("page-indent") {
              h2 { +"srcref Version" }
              p { +"Version: $VERSION" }
              p { +"Release Date: $RELEASE_DATE" }
            }
          }
        }
      }.serialize()
    }
  }
}
