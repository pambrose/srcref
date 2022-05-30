package com.pambrose.srcref.pages

import com.github.pambrose.common.response.*
import com.github.pambrose.srcref.srcref.BuildConfig.RELEASE_DATE
import com.github.pambrose.srcref.srcref.BuildConfig.VERSION
import com.pambrose.srcref.*
import com.pambrose.srcref.pages.Common.commonHead
import com.pambrose.srcref.pages.Common.githubIcon
import kotlinx.html.*
import kotlinx.html.dom.*

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
            div {
              style = "padding-left: 20px; padding-top: 40px;"
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