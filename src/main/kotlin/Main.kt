import QueryArgs.ACCOUNT
import QueryArgs.BRANCH
import QueryArgs.PATH
import QueryArgs.REPO
import SrcRef.githubRefUrl
import SrcRef.githubref
import SrcRef.logger
import SrcRef.queryParams
import SrcRef.urlPrefix
import com.github.pambrose.common.response.*
import com.github.pambrose.common.util.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.html.dom.*

fun main() {
  embeddedServer(CIO, port = System.getenv("PORT")?.toInt() ?: 8080) {
    install(CallLogging)
    install(DefaultHeaders) { header("X-Engine", "Ktor") }
    install(Compression) {
      gzip { priority = 1.0 }
      deflate { priority = 10.0; minimumSize(1024) /* condition*/ }
    }

    routing {
      get("/") {
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
                                // here the magic happens
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
                          name = ACCOUNT.arg; size = "20"; required = true; value = params[ACCOUNT.arg] ?: ""
                        }
                      }
                    }
                    tr {
                      td { +"Repo Name:" }
                      td { textInput { name = REPO.arg; size = "20"; required = true; value = params[REPO.arg] ?: "" } }
                    }
                    tr {
                      td { +"Branch Name:" }
                      val pv = (params[BRANCH.arg] ?: "").let { if (it.isBlank()) "master" else it }
                      td { textInput { name = BRANCH.arg; size = "20"; required = true; value = pv } }
                    }
                    tr {
                      td { +"File Path:" }
                      val pv = (params[PATH.arg] ?: "").let { if (it.isBlank()) "/src/main/kotlin/" else it }
                      td { textInput { name = PATH.arg; size = "70"; required = true; value = pv } }
                    }
                    tr {
                      td { +"Match Expr:" }
                      td {
                        //val pv = params[REGEX.arg] ?: ""
                        textInput { name = "regex"/*REGEX.arg*/; size = "20"; required = true; value = "pv" }
                      }
                    }
//                    tr {
//                      td {  +"Offset:"  }
//                      val pv = (params[OFFSET.arg] ?: "").let { if (it.isBlank()) "0" else it }
//                      td { textInput { name = OFFSET.arg; size = "10"; required = true; value = pv } }
//                    }
//                    tr {
//                      td {  +"occurrence:"  }
//                      td {
//                        val pv = (params[OCCURRENCE.arg] ?: "").let { if (it.isBlank()) "1" else it }
//                        val isSelected = pv.toInt()
//                        select {
//                          name = OCCURRENCE.arg
//                          size = "1"
//                          option { +" 1st "; value = "1"; selected = isSelected == 1 }
//                          option { +" 2nd "; value = "2"; selected = isSelected == 2 }
//                          option { +" 3rd "; value = "3"; selected = isSelected == 3 }
//                          option { +" 4th "; value = "4"; selected = isSelected == 4 }
//                          option { +" 5th "; value = "5"; selected = isSelected == 5 }
//                          option { +" 6th "; value = "6"; selected = isSelected == 6 }
//                          option { +" 7th "; value = "7"; selected = isSelected == 7 }
//                          option { +" 8th "; value = "8"; selected = isSelected == 8 }
//                          option { +" 9th "; value = "9"; selected = isSelected == 9 }
//                          option { +" 10th "; value = "10"; selected = isSelected == 10 }
//                        }
//                      }
//                    }
//                    tr {
//                      td {  +"Search Direction:" }
//                      td {
//                        span {
//                          val pv = (params[TOPDOWN.arg] ?: "").let { if (it.isBlank()) "true" else it }
//                          val isChecked = pv.toBoolean()
//                          style = "text-align:center"
//                          radioInput { id = "topdown"; name = TOPDOWN.arg; value = "true"; checked = isChecked }
//                          label {
//                            htmlFor = "topdown"; +" Top-down "
//                          }
//                          radioInput { id = "bottomup"; name = TOPDOWN.arg; value = "false"; checked = !isChecked }
//                          label { htmlFor = "bottomup"; +" Bottom-up " }
//                        }
//                      }
//                    }
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

                if (params.values.asSequence().filter { it.isNotBlank() }.any()) {
                  val args = params.map { (k, v) -> "$k=${v.encode()}" }.joinToString("&")
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

      get(githubref) {
        val params = queryParams.onEach { (k, v) -> logger.info { "$k=$v" } }
        redirectTo { githubRefUrl(params) }
      }
    }
  }.start(wait = true)
}
