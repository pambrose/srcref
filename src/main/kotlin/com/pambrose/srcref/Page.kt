package com.pambrose.srcref

import com.github.pambrose.common.response.*
import com.pambrose.srcref.QueryArgs.ACCOUNT
import com.pambrose.srcref.QueryArgs.BRANCH
import com.pambrose.srcref.QueryArgs.OCCURRENCE
import com.pambrose.srcref.QueryArgs.OFFSET
import com.pambrose.srcref.QueryArgs.PATH
import com.pambrose.srcref.QueryArgs.REGEX
import com.pambrose.srcref.QueryArgs.REPO
import com.pambrose.srcref.QueryArgs.TOPDOWN
import com.pambrose.srcref.Utils.githubRefUrl
import com.pambrose.srcref.Utils.rawHtml
import com.pambrose.srcref.Utils.srcRefUrl
import io.ktor.http.ContentType.Text.CSS
import kotlinx.html.*
import kotlinx.html.dom.*

object Page {
  private val urlPrefix = (System.getenv("PREFIX") ?: "http://localhost:8080").removeSuffix("/")

  internal suspend fun PipelineCall.displayForm(params: Map<String, String?>) {
    respondWith {
      document {
        append.html {
          head {
            meta { charset = "utf-8" }
            meta { name = "apple-mobile-web-app-capable"; content = "yes" }
            meta { name = "apple-mobile-web-app-status-bar-style"; content = "black-translucent" }
            meta {
              name = "viewport"
              content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
            }

            link { rel = "stylesheet"; href = "css/srcref.css"; type = CSS.toString() }
            script { src = "js/copyUrl.js" }
            title { +"srcref" }
          }
          body {
            a(href = "https://github.com/pambrose/srcref", target = "_blank", classes = "top-right") {
              title = "View source on GitHub"
              rawHtml(
                """
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 55 55">
                    <path fill="currentColor" stroke="none" d="M27.5 11.2a16.3 16.3 0 0 0-5.1 31.7c.8.2 1.1-.3 1.1-.7v-2.8c-4.5 1-5.5-2.2-5.5-2.2-.7-1.9-1.8-2.4-1.8-2.4-1.5-1 .1-1 .1-1 1.6.1 2.5 1.7 2.5 1.7 1.5 2.5 3.8 1.8 4.7 1.4.2-1 .6-1.8 1-2.2-3.5-.4-7.3-1.8-7.3-8 0-1.8.6-3.3 1.6-4.4-.1-.5-.7-2.1.2-4.4 0 0 1.4-.4 4.5 1.7a15.6 15.6 0 0 1 8.1 0c3.1-2 4.5-1.7 4.5-1.7.9 2.3.3 4 .2 4.4 1 1 1.6 2.6 1.6 4.3 0 6.3-3.8 7.7-7.4 8 .6.6 1.1 1.6 1.1 3v4.6c0 .4.3.9 1.1.7a16.3 16.3 0 0 0-5.2-31.7"></path>
                  </svg>
                """
              )
            }

            div {
              style = "padding-left: 25px; padding-top: 25px;"
              h2 { +"srcref - Dynamic Line-Specific GitHub Permalinks" }
            }

            form {
              action = "/"
              method = FormMethod.get
              table {
                tr {
                  td { +"Org Name/Username:" }
                  td {
                    textInput {
                      name = ACCOUNT.arg; size = "30"; required = true; value = ACCOUNT.defaultIfNull(params)
                    }
                  }
                }
                tr {
                  td { +"Repo Name:" }
                  td {
                    textInput { name = REPO.arg; size = "30"; required = true; value = REPO.defaultIfNull(params) }
                  }
                }
                tr {
                  td { +"Branch Name:" }
                  td {
                    textInput { name = BRANCH.arg; size = "30"; required = true; value = BRANCH.defaultIfNull(params) }
                  }
                }
                tr {
                  td { +"File Path:" }
                  td {
                    textInput { name = PATH.arg; size = "70"; required = true; value = PATH.defaultIfNull(params) }
                  }
                }
                tr {
                  td { +"Regex:" }
                  td {
                    textInput { name = REGEX.arg; size = "30"; required = true; value = REGEX.defaultIfNull(params) }
                  }
                }
                tr {
                  td { +"Occurrence:" }
                  td {
                    val isSelected = OCCURRENCE.defaultIfBlank(params).toInt()
                    select {
                      name = OCCURRENCE.arg
                      size = "1"
                      option { +" 1st "; value = "1"; selected = isSelected == 1 }
                      option { +" 2nd "; value = "2"; selected = isSelected == 2 }
                      option { +" 3rd "; value = "3"; selected = isSelected == 3 }
                      option { +" 4th "; value = "4"; selected = isSelected == 4 }
                      option { +" 5th "; value = "5"; selected = isSelected == 5 }
                      option { +" 6th "; value = "6"; selected = isSelected == 6 }
                      option { +" 7th "; value = "7"; selected = isSelected == 7 }
                      option { +" 8th "; value = "8"; selected = isSelected == 8 }
                      option { +" 9th "; value = "9"; selected = isSelected == 9 }
                      option { +" 10th "; value = "10"; selected = isSelected == 10 }
                    }
                  }
                }
                tr {
                  td { +"Offset:" }
                  td {
                    textInput { name = OFFSET.arg; size = "10"; required = true; value = OFFSET.defaultIfNull(params) }
                  }
                }
                tr {
                  td { +"Search Direction:" }
                  td {
                    span {
                      val isChecked = TOPDOWN.defaultIfBlank(params).toBoolean()
                      style = "text-align:center"
                      radioInput { id = "topdown"; name = TOPDOWN.arg; value = "true"; checked = isChecked }
                      label {
                        htmlFor = "topdown"; +" Top-down "
                      }
                      radioInput { id = "bottomup"; name = TOPDOWN.arg; value = "false"; checked = !isChecked }
                      label { htmlFor = "bottomup"; +" Bottom-up " }
                    }
                  }
                }
                tr {
                  td { }
                  td {
                    style = "padding-top:10"
                    submitInput {
                      style = "font-size:25px; height:35; vertical-align:middle;"
                      value = "Generate URL"
                    }
                  }
                }
              }
            }

            if (params.values.asSequence().filter { it?.isNotBlank() ?: false }.any()) {
              val srcrefUrl = srcRefUrl(params, prefix = urlPrefix)
              val githubUrl = githubRefUrl(params)

              div {
                style = "padding-left: 25px;"
                br {}
                p { +"Embed this URL in your docs:" }
                textArea { id = "srcrefUrl"; rows = "3"; +srcrefUrl; cols = "91"; readonly = true }
                p { +"to reach this GitHub page:" }
                textArea { rows = "2"; +githubUrl; cols = "91"; readonly = true }
                p {}
                button { onClick = "copyUrl()"; +"Copy URL" }
                span { +" " }
                button(classes = "btn btn-success") {
                  onClick = "window.open('$srcrefUrl','_blank')"
                  +"View Permalink"
                }
              }
            }
          }
        }
      }.serialize()
    }
  }
}