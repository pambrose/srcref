import QueryArgs.ACCOUNT
import QueryArgs.BRANCH
import QueryArgs.OCCURENCE
import QueryArgs.OFFSET
import QueryArgs.PATH
import QueryArgs.REGEX
import QueryArgs.REPO
import QueryArgs.TOPDOWN
import Target.calcLineNumber
import Target.logger
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
  val geturl = "getUrl"
  val prefix = "http://localhost:8080/"
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
              body {
                form {
                  action = geturl
                  method = FormMethod.get
                  table {
                    tr {
                      td { style = ""; label { +"GitHub Username or Org:" } }
                      td { textInput { name = ACCOUNT.arg; size = "20" } }
                    }
                    tr {
                      td { style = ""; label { +"Repo Name:" } }
                      td { textInput { name = REPO.arg; size = "20" } }
                    }
                    tr {
                      td { style = ""; label { +"Git Branch:" } }
                      td { textInput { name = BRANCH.arg; size = "20"; value = "master" } }
                    }
                    tr {
                      td { style = ""; label { +"File Path:" } }
                      td { textInput { name = PATH.arg; size = "70"; value = "/src/main/kotlin/..." } }
                    }
                    tr {
                      td { style = ""; label { +"Match Expr:" } }
                      td { textInput { name = REGEX.arg; size = "20" } }
                    }
                    tr {
                      td { style = ""; label { +"Offset:" } }
                      td { textInput { name = OFFSET.arg; size = "10"; value = "0" } }
                    }
                    tr {
                      td { style = ""; label { +"Occurence:" } }
                      td {
                        select {
                          name = OCCURENCE.arg
                          size = "1"
                          option { +" 1st "; value = "1" }
                          option { +" 2nd "; value = "2" }
                          option { +" 3rd "; value = "3" }
                          option { +" 4th "; value = "4" }
                          option { +" 5th "; value = "5" }
                          option { +" 6th "; value = "6" }
                          option { +" 7th "; value = "7" }
                          option { +" 8th "; value = "8" }
                          option { +" 9th "; value = "9" }
                          option { +" 10th "; value = "10" }
                        }
                      }
                    }
                    tr {
                      td { style = ""; label { +"Search Direction:" } }
                      td {
                        span {
                          style = "text-align:center"
                          radioInput {
                            id = "topdown"
                            name = TOPDOWN.arg
                            value = "true"
                            checked = true
                          }
                          label {
                            htmlFor = "topdown"
                            +" Top Down "
                          }
                          radioInput {
                            id = "bottomup"
                            name = TOPDOWN.arg
                            value = "false"
                            checked = false
                          }
                          label {
                            htmlFor = "bottomup"
                            +" Bottom Up "
                          }
                        }
                      }
                    }
                    tr {
                      td { }
                      td {
                        style = "padding-top:10"
                        submitInput {
                          style = "font-size:25px; height:35; width:  155"
                          value = "Get srcref URL"
                        }
                      }
                    }
                  }
                }
              }
            }
          }.serialize()
        }
      }

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

      get(geturl) {
        respondWith {
          document {
            append.html {
              body {
                val args = getVals().map { (k, v) -> "$k=${v.encode()}" }.joinToString("&")
                val url = "$prefix$srcurl?$args"
                a { href = url; target = "_blank"; +"Test URL" }
              }
            }
          }.serialize()
        }
      }

      get(srcurl) {
        val vals = getVals().apply { forEach { (k, v) -> logger.info { "$k=$v" } } }

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

        redirectTo { githubSourceUrl(account, repo, path, branch, linenum) }
      }
    }
  }.start(wait = true)
}

object Target : KLogging() {
  fun calcLineNumber(rawLines: List<String>, topDown: Boolean, pattern: String, occurence: Int, offset: Int) =
    try {
      val lines = if (topDown) rawLines else rawLines.asReversed()
      val cnt = lines.size
      val regex = Regex(pattern)
      (lines
        .asSequence()
        .mapIndexed { index, s -> (if (topDown) index else (cnt - index - 1)) to s.contains(regex) }
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
