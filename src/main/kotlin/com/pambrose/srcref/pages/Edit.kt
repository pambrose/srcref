package com.pambrose.srcref.pages

import com.pambrose.srcref.Api
import com.pambrose.srcref.Endpoints.EDIT
import com.pambrose.srcref.Endpoints.WHAT
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
import com.pambrose.srcref.Urls
import com.pambrose.srcref.pages.Common.SrcRefDslTag
import com.pambrose.srcref.pages.Common.URL_PREFIX
import com.pambrose.srcref.pages.Common.WIDTH_VAL
import com.pambrose.srcref.pages.Common.hasValues
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.routing.RoutingContext
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.FormMethod
import kotlinx.html.SELECT
import kotlinx.html.TABLE
import kotlinx.html.TD
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.label
import kotlinx.html.onClick
import kotlinx.html.option
import kotlinx.html.radioInput
import kotlinx.html.script
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.submitInput
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.textArea
import kotlinx.html.textInput
import kotlinx.html.tr

object Edit {
  internal suspend fun RoutingContext.displayEdit(params: Map<String, String?>) {
    // This is called early because it is suspending, and we cannot suspend inside document construction
    val (githubUrl, msg) = Urls.githubRangeUrl(params, URL_PREFIX)

    call.respondHtmlTemplate(PageTemplate("srcref")) {
      script {
        script { src = "js/copyUrl.js" }
      }
      content {
        div("page-indent") {
          h2 {
            span {
              +"srcref - Dynamic Line-Specific GitHub Permalinks"
              a {
                href = "/$WHAT"
                style = "padding-left: 25px; font-size: 75%;"
                +"(What is srcref?)"
              }
            }
          }

          val textWidth = "40"
          val offsetWidth = "6"
          form {
            action = "/$EDIT"
            method = FormMethod.get
            table {
              formElement("Username/Org Name:", "GitHub username or organization name") {
                textInput {
                  name = ACCOUNT.arg
                  size = textWidth
                  required = true
                  value = ACCOUNT.defaultIfNull(params)
                }
              }
              formElement("Repo Name:", "GitHub repository name") {
                textInput {
                  name = REPO.arg
                  size = textWidth
                  required = true
                  value = REPO.defaultIfNull(params)
                }
              }
              formElement("Branch Name:", "GitHub branch name") {
                textInput {
                  name = BRANCH.arg
                  size = textWidth
                  required = true
                  value = BRANCH.defaultIfNull(params)
                }
              }
              formElement("File Path:", "File path in repository") {
                textInput {
                  name = PATH.arg
                  size = "70"
                  required = true
                  value = PATH.defaultIfNull(params)
                }
              }
              formElement("Begin Regex:", "Regex used to determine the beginning match") {
                textInput {
                  name = BEGIN_REGEX.arg
                  size = textWidth
                  required = true
                  value = BEGIN_REGEX.defaultIfNull(params)
                }
              }
              formElement("Begin Occurrence:", "Number of matches for the beginning match") {
                val isSelected = BEGIN_OCCURRENCE.defaultIfBlank(params).toInt()
                select("occurrence") {
                  name = BEGIN_OCCURRENCE.arg
                  size = "1"
                  occurrenceOptions(isSelected)
                }
              }
              formElement("Begin Offset:", "Number of lines above or below the beginning match") {
                textInput {
                  name = BEGIN_OFFSET.arg
                  size = offsetWidth
                  required = true
                  value = BEGIN_OFFSET.defaultIfNull(params)
                }
              }
              formElement("Begin Search Direction:", "Direction to evaluate the file for the beginning match") {
                span {
                  val isChecked = BEGIN_TOPDOWN.defaultIfBlank(params).toBoolean()
                  style = "text-align:center"
                  "begin_topdown".also { lab ->
                    radioInput {
                      id = lab
                      name = BEGIN_TOPDOWN.arg
                      value = "true"
                      checked = isChecked
                    }
                    label {
                      htmlFor = lab
                      +" Top-down "
                    }
                  }
                  "begin_bottomup".also { lab ->
                    radioInput {
                      id = lab
                      name = BEGIN_TOPDOWN.arg
                      value = "false"
                      checked = !isChecked
                    }
                    label {
                      htmlFor = lab
                      +" Bottom-up "
                    }
                  }
                }
              }
              formElement("End Regex:", "Optional regex used to determine the ending match") {
                textInput {
                  name = END_REGEX.arg
                  size = textWidth
                  value = END_REGEX.defaultIfNull(params)
                }
              }
              formElement("End Occurrence:", "Optional number of matches for the ending match") {
                val isSelected = END_OCCURRENCE.defaultIfBlank(params).toInt()
                select("occurrence") {
                  name = END_OCCURRENCE.arg
                  size = "1"
                  occurrenceOptions(isSelected)
                }
              }
              formElement("End Offset:", "Optional number of lines above or below the ending match") {
                textInput {
                  name = END_OFFSET.arg
                  size = offsetWidth
                  value = END_OFFSET.defaultIfNull(params)
                }
              }
              formElement("End Search Direction:", "Optional direction to evaluate the file for the ending match") {
                span {
                  val isChecked = END_TOPDOWN.defaultIfBlank(params).toBoolean()
                  style = "text-align:center"
                  "end_topdown".also { lab ->
                    radioInput {
                      id = lab
                      name = END_TOPDOWN.arg
                      value = "true"
                      checked = isChecked
                    }
                    label {
                      htmlFor = lab
                      +" Top-down "
                    }
                  }
                  "end_bottomup".also { lab ->
                    radioInput {
                      id = lab
                      name = END_TOPDOWN.arg
                      value = "false"
                      checked = !isChecked
                    }
                    label {
                      htmlFor = lab
                      +" Bottom-up "
                    }
                  }
                }
              }
              formElement("") {
                style = "padding-top:10"
                submitInput(classes = "button") { value = "Generate URL" }
              }
            }
          }

          div {
            style = "padding-top: 15px"
            if (params.hasValues()) {
              val srcrefUrl = Urls.srcrefToGithubUrl(params, prefix = URL_PREFIX)
              val isValid = msg.isEmpty()
              span {
                button(classes = "button") {
                  onClick = "window.open('$EDIT', '_self')"
                  +"Reset Values"
                }
                if (isValid) {
                  button(classes = "button") {
                    style = "margin-left: 10px;"
                    onClick = "copyUrl()"
                    +"Copy URL"
                  }
                  button(classes = "button") {
                    style = "margin-left: 10px;"
                    onClick = "window.open('$srcrefUrl', '_blank')"
                    +"View GitHub Permalink"
                  }
                  div {
                    id = "snackbar"
                    +"URL Copied!"
                  }
                }
              }

              if (isValid) {
                div {
                  style = "padding-top: 17px; padding-bottom: 10px;"
                  +"Embed this URL in your docs:"
                }
                textArea {
                  id = "srcrefUrl"
                  rows = "4"
                  cols = WIDTH_VAL
                  readonly = true
                  +srcrefUrl
                }
                div {
                  style = "padding-top: 10px; padding-bottom: 10px;"
                  +"To dynamically generate this GitHub permalink:"
                }
                textArea {
                  rows = "2"
                  cols = WIDTH_VAL
                  readonly = true
                  +githubUrl
                }
              } else {
                h2 { +"Exception:" }
                textArea {
                  rows = "3"
                  cols = WIDTH_VAL
                  readonly = true
                  +msg
                }
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
                    endOffset = 6,
                    endTopDown = true,
                    prefix = "",
                  )
                onClick = "window.open('${"$url&edit=true"}','_self')"
                +"Example Values"
              }
            }
          }
        }
      }
    }
  }

  @SrcRefDslTag
  private inline fun TABLE.formElement(
    label: String,
    crossinline block: TD.() -> Unit,
  ) = tr {
    td { +label }
    td {
      block()
    }
  }

  @SrcRefDslTag
  internal inline fun TABLE.formElement(
    label: String,
    tooltip: String,
    crossinline block: FlowOrPhrasingContent.() -> Unit,
  ) = formElement(label) {
    withToolTip(tooltip) {
      block()
    }
  }

  @SrcRefDslTag
  private inline fun FlowOrPhrasingContent.withToolTip(
    msg: String,
    crossinline block: FlowOrPhrasingContent.() -> Unit,
  ) = span {
    block()
    span("tooltip") {
      span("spacer") {}
      img {
        src = "images/question.png"
        width = "18"
        height = "18"
      }
      span("tooltiptext") { +msg }
    }
  }

  private fun SELECT.occurrenceOptions(isSelected: Int) {
    listOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th").forEachIndexed { index, s ->
      option {
        value = (index + 1).toString()
        selected = isSelected == (index + 1)
        +" $s "
      }
    }
  }
}
