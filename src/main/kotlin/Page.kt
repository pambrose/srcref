import QueryArgs.ACCOUNT
import QueryArgs.BRANCH
import QueryArgs.OCCURRENCE
import QueryArgs.OFFSET
import QueryArgs.PATH
import QueryArgs.REGEX
import QueryArgs.REPO
import QueryArgs.TOPDOWN
import SrcRef.githubRefUrl
import SrcRef.githubref
import SrcRef.queryParams
import SrcRef.urlPrefix
import com.github.pambrose.common.response.*
import com.github.pambrose.common.util.*
import kotlinx.html.*
import kotlinx.html.dom.*

object Page {
  suspend fun PipelineCall.displayForm() {
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
            script {
              rawHtml("\n")
              +"""
                function copyToClipboard(textToCopy) {
                    // navigator clipboard api needs a secure context (https)
                    if (navigator.clipboard && window.isSecureContext) {
                        // navigator clipboard api method'
                        return navigator.clipboard.writeText(textToCopy);
                    } else {
                        // text area method
                        let textArea = document.createElement("textarea");
                        textArea.value = textToCopy;
                        // make the textarea out of viewport
                        textArea.style.position = "fixed";
                        textArea.style.left = "-999999px";
                        textArea.style.top = "-999999px";
                        document.body.appendChild(textArea);
                        textArea.focus();
                        textArea.select();
                        return new Promise((res, rej) => {
                            document.execCommand('copy') ? res() : rej();
                            textArea.remove();
                        });
                    }
                }
  
                function copyUrl() {
                  var copyText = document.getElementById("urlval");
                  copyText.select();
                  copyToClipboard(copyText.value);
                  //.then(() => alert("Copied the text: " + copyText.value));
                }
              """.trimIndent().prependIndent("\t\t")
              rawHtml("\n\t\t")
            }

            rawHtml("\n")
            style("text/css") {
              media = "screen"
              rawHtml("\n")
              +"""
                form {
                  padding: 25px;
                }
                
                table {
                    border-collapse: collapse;
                    border: 2px solid black;
                }

                td {
                  padding: 5px;
                }
              """.trimIndent().prependIndent("\t\t")
              rawHtml("\n\t")
            }
            rawHtml("\n")
          }
          body {
            val params = queryParams

            form {
              action = "/"
              method = FormMethod.get
              table {
                tr {
                  td { +"Org Name/Username:" }
                  td {
                    textInput {
                      name = ACCOUNT.arg; size = "20"; required = true; value = ACCOUNT.defaultIfNull(params)
                    }
                  }
                }
                tr {
                  td { +"Repo Name:" }
                  td {
                    textInput { name = REPO.arg; size = "20"; required = true; value = REPO.defaultIfNull(params) }
                  }
                }
                tr {
                  td { +"Branch Name:" }
                  td {
                    textInput { name = BRANCH.arg; size = "20"; required = true; value = BRANCH.defaultIfNull(params) }
                  }
                }
                tr {
                  td { +"File Path:" }
                  td {
                    textInput { name = PATH.arg; size = "70"; required = true; value = PATH.defaultIfNull(params) }
                  }
                }
                tr {
                  td { +"Match Expr:" }
                  td {
                    textInput { name = REGEX.arg; size = "20"; required = true; value = REGEX.defaultIfNull(params) }
                  }
                }
                tr {
                  td { +"Offset:" }
                  td {
                    textInput { name = OFFSET.arg; size = "10"; required = true; value = OFFSET.defaultIfNull(params) }
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
                      value = "Get URL"
                    }
                  }
                }
              }
            }

            if (params.values.asSequence().filter { it?.isNotBlank() ?: false }.any()) {
              val args =
                params
                  .map { (k, v) -> if (v.isNotNull()) "$k=${v.encode()}" else "" }
                  .filter { it.isNotBlank() }
                  .joinToString("&")
              val url = "$urlPrefix/$githubref?$args"
              val ghurl = githubRefUrl(params)

              div {
                style = "padding-left: 25px;"
                br {}
                textArea { id = "urlval"; rows = "3"; +url; cols = "91"; readonly = true }
                p { +"will redirect to:" }
                textArea { id = "ghurlval"; rows = "1"; +ghurl; cols = "91"; readonly = true }
                p {}
                button { onClick = "copyUrl()"; +"Copy URL" }
                span { +" " }
                button(classes = "btn btn-success") {
                  onClick = "window.open('$url','_blank')"
                  +"Try it!"
                }
              }
            }
          }
        }
      }.serialize()
    }
  }
}

fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }