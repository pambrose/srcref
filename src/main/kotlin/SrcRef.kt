import QueryArgs.ACCOUNT
import QueryArgs.BRANCH
import QueryArgs.OCCURENCE
import QueryArgs.OFFSET
import QueryArgs.PATH
import QueryArgs.REGEX
import QueryArgs.REPO
import QueryArgs.TOPDOWN
import SrcRef.getVals
import SrcRef.githubRefUrl
import SrcRef.logger
import com.github.pambrose.common.response.*
import com.github.pambrose.common.util.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import kotlinx.html.dom.*
import mu.*
import java.net.*

enum class QueryArgs {
  ACCOUNT, REPO, BRANCH, PATH, REGEX, OFFSET, OCCURENCE, TOPDOWN;

  val arg get() = name.lowercase()
}

fun main() {
  val prefix = (System.getenv("PREFIX") ?: "http://localhost:8080").removeSuffix("/")
  val srcurl = "srcurl"

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
                val vals = getVals()

                form {
                  action = "/"
                  method = FormMethod.get
                  table {
                    tr {
                      td { style = ""; label { +"Org Name/Username:" } }
                      td { textInput { name = ACCOUNT.arg; size = "20"; value = vals[ACCOUNT.arg] ?: "" } }
                    }
                    tr {
                      td { style = ""; label { +"Repo Name:" } }
                      td { textInput { name = REPO.arg; size = "20"; value = vals[REPO.arg] ?: "" } }
                    }
                    tr {
                      td { style = ""; label { +"Branch Name:" } }
                      td { textInput { name = BRANCH.arg; size = "20"; value = vals[BRANCH.arg] ?: "master" } }
                    }
                    tr {
                      td { style = ""; label { +"File Path:" } }
                      td { textInput { name = PATH.arg; size = "70"; value = vals[PATH.arg] ?: "/src/main/kotlin/" } }
                    }
                    tr {
                      td { style = ""; label { +"Match Expr:" } }
                      td { textInput { name = REGEX.arg; size = "20"; value = vals[REGEX.arg] ?: "" } }
                    }
                    tr {
                      td { style = ""; label { +"Offset:" } }
                      td { textInput { name = OFFSET.arg; size = "10"; value = vals[OFFSET.arg] ?: "0" } }
                    }
                    tr {
                      td { style = ""; label { +"Occurence:" } }
                      td {
                        select {
                          name = OCCURENCE.arg
                          size = "1"
                          val isSelected = (vals[OCCURENCE.arg] ?: "1").toInt()
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
                      td { style = ""; label { +"Search Direction:" } }
                      td {
                        span {
                          val isChecked = (vals[TOPDOWN.arg] ?: "true").toBoolean()
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

                if (vals.values.asSequence().filter { it.isNotBlank() }.any()) {
                  val args = vals.map { (k, v) -> "$k=${v.encode()}" }.joinToString("&")
                  val url = githubRefUrl(vals)//"$prefix/?$args"

                  div {
                    style = "padding-left: 25px;"
                    br {}
                    input { id = "urlval"; type = InputType.text; value = url; size = "95px" }
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

      get(srcurl) {
        val vals = getVals().apply { forEach { (k, v) -> logger.info { "$k=$v" } } }
        redirectTo { githubRefUrl(vals) }
      }
    }
  }.start(wait = true)
}

object SrcRef : KLogging() {

  fun PipelineContext<Unit, ApplicationCall>.getVals() =
    mutableMapOf<String, String>()
      .also {
        QueryArgs
          .values()
          .map { it.arg }
          .forEach { arg ->
            it[arg] = call.request.queryParameters[arg] ?: ""
          }
      }

  fun githubRefUrl(vals: Map<String, String>): String {
    val account = vals[ACCOUNT.arg] ?: ""
    val repo = vals[REPO.arg] ?: ""
    val path = vals[PATH.arg] ?: ""
    val branch = vals[BRANCH.arg] ?: ""

    val url = githubRawUrl(account, repo, path, branch)
    val lines = URL(url).readText().lines()

    val linenum =
      calcLineNumber(
        lines,
        vals[TOPDOWN.arg]?.toBoolean() ?: true,
        vals[REGEX.arg] ?: "",
        vals[OCCURENCE.arg]?.toInt() ?: 1,
        vals[OFFSET.arg]?.toInt() ?: 0
      )
    return githubSourceUrl(account, repo, path, branch, linenum)
  }

  fun calcLineNumber(lines: List<String>, topDown: Boolean, pattern: String, occurence: Int, offset: Int) =
    try {
      val regex = Regex(pattern)
      ((if (topDown) lines else lines.asReversed())
        .asSequence()
        .mapIndexed { index, s -> (if (topDown) index else (lines.size - index - 1)) to s.contains(regex) }
        .filter { it.second }
        .drop(occurence - 1)
        .first().first) + offset + 1
    } catch (e: Throwable) {
      logger.info(e) { "Error in calcLineNumber()" }
      1
    }
}

fun githubSourceUrl(
  username: String,
  repoName: String,
  path: String = "",
  branchName: String = "master",
  lineNum: Int
) =
  "https://github.com/$username/$repoName/blob/$branchName/$path#L$lineNum"

fun githubRawUrl(username: String, repoName: String, path: String = "", branchName: String = "master") =
  "https://raw.githubusercontent.com/$username/$repoName/$branchName/$path"

fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }
