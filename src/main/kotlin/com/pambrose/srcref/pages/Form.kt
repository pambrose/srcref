package com.pambrose.srcref.pages

import com.github.pambrose.common.response.*
import com.pambrose.srcref.*
import com.pambrose.srcref.Endpoints.EDIT
import com.pambrose.srcref.QueryParams.ACCOUNT
import com.pambrose.srcref.QueryParams.BEGIN_OCCURRENCE
import com.pambrose.srcref.QueryParams.BEGIN_OFFSET
import com.pambrose.srcref.QueryParams.BEGIN_REGEX
import com.pambrose.srcref.QueryParams.BEGIN_TOPDOWN
import com.pambrose.srcref.QueryParams.BRANCH
import com.pambrose.srcref.QueryParams.END_OCCURRENCE
import com.pambrose.srcref.QueryParams.END_OFFSET
import com.pambrose.srcref.QueryParams.END_REGEX
import com.pambrose.srcref.QueryParams.END_TOPDOWN
import com.pambrose.srcref.QueryParams.PATH
import com.pambrose.srcref.QueryParams.REPO
import com.pambrose.srcref.pages.Common.commonHead
import com.pambrose.srcref.pages.Common.githubIcon
import com.pambrose.srcref.pages.Common.hasValues
import com.pambrose.srcref.pages.Common.urlPrefix
import com.pambrose.srcref.pages.Common.widthVal
import kotlinx.html.*
import kotlinx.html.dom.*

object Form {
  internal suspend fun PipelineCall.displayForm(params: Map<String, String?>) {

    // This is called early because it is suspending and we cannot suspend inside document construction
    val (githubUrl, errorMsg) = Urls.githubRangeUrl(params, urlPrefix)

    respondWith {
      document {
        append.html {
          head {
            commonHead()
            script { src = "js/copyUrl.js" }
            title { +"srcref" }
          }
          body {
            githubIcon()

            div {
              style = "padding-left: 25px; padding-top: 10px;"
              h2 { +"srcref - Dynamic Line-Specific GitHub Permalinks" }
            }

            val textWidth = "40"
            val offsetWidth = "6"
            form {
              action = "/$EDIT"
              method = FormMethod.get
              table {
                tr {
                  td { +"Username/Org Name:" }
                  td {
                    withToolTop("GitHub username or organization name") {
                      textInput {
                        name = ACCOUNT.arg; size = textWidth; required = true
                        value = ACCOUNT.defaultIfNull(params)
                      }
                    }
                  }
                }
                tr {
                  td { +"Repo Name:" }
                  td {
                    withToolTop("GitHub repository name") {
                      textInput {
                        name = REPO.arg; size = textWidth; required = true
                        value = REPO.defaultIfNull(params)
                      }
                    }
                  }
                }
                tr {
                  td { +"Branch Name:" }
                  td {
                    withToolTop("GitHub branch name") {
                      textInput {
                        name = BRANCH.arg; size = textWidth; required = true
                        value = BRANCH.defaultIfNull(params)
                      }
                    }
                  }
                }
                tr {
                  td { +"File Path:" }
                  td {
                    withToolTop("File path in repository") {
                      textInput {
                        name = PATH.arg; size = "70"; required = true
                        value = PATH.defaultIfNull(params)
                      }
                    }
                  }
                }
                tr {
                  td { +"Begin Regex:" }
                  td {
                    withToolTop("Regex used to determine the beginning match") {
                      textInput {
                        name = BEGIN_REGEX.arg; size = textWidth; required = true
                        value = BEGIN_REGEX.defaultIfNull(params)
                      }
                    }
                  }
                }
                tr {
                  td { +"Begin Occurrence:" }
                  td {
                    withToolTop("Number of matches for the beginning match") {
                      val isSelected = BEGIN_OCCURRENCE.defaultIfBlank(params).toInt()
                      select("occurrence") { name = BEGIN_OCCURRENCE.arg; size = "1"; occurrenceOptions(isSelected) }
                    }
                  }
                }

                tr {
                  td { +"Begin Offset: " }
                  td {
                    withToolTop("Number of lines above or below the beginning match") {
                      textInput {
                        name = BEGIN_OFFSET.arg; size = offsetWidth; required = true
                        value = BEGIN_OFFSET.defaultIfNull(params)
                      }
                    }
                  }
                }
                tr {
                  td { +"Begin Search Direction:" }
                  td {
                    withToolTop("Direction to evaluate the file for the beginning match") {
                      span {
                        val isChecked = BEGIN_TOPDOWN.defaultIfBlank(params).toBoolean()
                        style = "text-align:center"
                        radioInput {
                          id = "begin_topdown"; name = BEGIN_TOPDOWN.arg; value = "true"; checked = isChecked
                        }
                        label {
                          htmlFor = "begin_topdown"; +" Top-down "
                        }
                        radioInput {
                          id = "begin_bottomup"; name = BEGIN_TOPDOWN.arg; value = "false"; checked = !isChecked
                        }
                        label { htmlFor = "begin_bottomup"; +" Bottom-up " }
                      }
                    }
                  }
                }
//                tr {
//                  td { id = "optional" }
//                  td { id = "optional"; +"⬇ End values are optional ⬇" }
//                }
                tr {
                  td { +"End Regex:" }
                  td {
                    withToolTop("Optional regex used to determine the ending match") {
                      textInput {
                        name = END_REGEX.arg; size = textWidth; value = END_REGEX.defaultIfNull(params)
                      }
                    }
                  }
                }
                tr {
                  td { +"End Occurrence:" }
                  td {
                    withToolTop("Optional number of matches for the ending match") {
                      val isSelected = END_OCCURRENCE.defaultIfBlank(params).toInt()
                      select("occurrence") { name = END_OCCURRENCE.arg; size = "1"; occurrenceOptions(isSelected) }
                    }
                  }
                }
                tr {
                  td { +"End Offset:" }
                  td {
                    withToolTop("Optional number of lines above or below the ending match") {
                      textInput {
                        name = END_OFFSET.arg; size = offsetWidth; value = END_OFFSET.defaultIfNull(params)
                      }
                    }
                  }
                }
                tr {
                  td { +"End Search Direction:" }
                  td {
                    withToolTop("Optional direction to evaluate the file for the ending match") {
                      span {
                        val isChecked = END_TOPDOWN.defaultIfBlank(params).toBoolean()
                        style = "text-align:center"
                        radioInput {
                          id = "end_topdown"; name = END_TOPDOWN.arg; value = "true"; checked = isChecked
                        }
                        label {
                          htmlFor = "end_topdown"; +" Top-down "
                        }
                        radioInput {
                          id = "end_bottomup"; name = END_TOPDOWN.arg; value = "false"; checked = !isChecked
                        }
                        label { htmlFor = "end_bottomup"; +" Bottom-up " }
                      }
                    }
                  }
                }
                tr {
                  td { }
                  td {
                    style = "padding-top:10"
                    submitInput(classes = "button") { value = "Generate URL" }
                  }
                }
              }
            }
            br {}
            div {
              style = "padding-left: 25px;"
              if (params.hasValues()) {
                val srcrefUrl = Urls.srcrefToGithubUrl(params, prefix = urlPrefix)
                val isValid = errorMsg.isEmpty()
                span {
                  button(classes = "button") {
                    onClick = "window.open('$EDIT', '_self')"
                    +"Reset Values"
                  }
                  if (isValid) {
                    div {
                      id = "snackbar"
                      +"URL Copied!"
                    }
                    +" "
                    button(classes = "button") { onClick = "copyUrl()"; +"Copy URL" }
                    +" "
                    button(classes = "button") {
                      onClick = "window.open('$srcrefUrl', '_blank')"
                      +"View GitHub Permalink"
                    }
                  }
                }

                if (isValid) {
                  p { +"Embed this URL in your docs:" }
                  textArea { id = "srcrefUrl"; rows = "4"; +srcrefUrl; cols = widthVal; readonly = true }
                  p { +"to reach this GitHub page:" }
                  textArea { rows = "2"; +githubUrl; cols = widthVal; readonly = true }
                } else {
                  h2 { +"Exception:" }
                  textArea { rows = "3"; cols = widthVal; readonly = true; +errorMsg }
                }
              } else {
                button(classes = "button") {
                  val url =
                    Api.srcrefUrl(
                      account = "pambrose",
                      repo = "srcref",
                      branch = "master",
                      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
                      beginRegex = "install\\(CallLogging\\)",
                      beginOccurrence = 1,
                      beginOffset = 0,
                      beginTopDown = true,
                      endRegex = "install\\(Compression\\)",
                      endOccurrence = 1,
                      endOffset = 3,
                      endTopDown = true,
                      prefix = "",
                    )
                  onClick =
                    "window.open('${"$url&edit=true"}','_self')"
                  +"Example Values"
                }
              }
            }
          }
        }
      }.serialize()
    }
  }

  private inline fun FlowOrPhrasingContent.withToolTop(
    msg: String,
    crossinline block: FlowOrPhrasingContent.() -> Unit
  ) {
    span {
      block()
      span("tooltip") {
        span("spacer") {}
        img { src = "images/question.png"; width = "18"; height = "18" }
        span("tooltiptext") { +msg }
      }
    }
  }

  private fun SELECT.occurrenceOptions(isSelected: Int) {
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