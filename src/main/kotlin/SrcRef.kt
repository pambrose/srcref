import Args.ACCOUNT
import Args.BRANCH
import Args.DIRECTION
import Args.OCCURENCE
import Args.OFFSET
import Args.PATH
import Args.REGEX
import Args.REPO
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
import java.net.*

enum class Args {
  ACCOUNT, REPO, BRANCH, PATH, REGEX, OFFSET, OCCURENCE, DIRECTION;

  val asArg get() = name.lowercase()
}

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
              body {
                form {
                  action = "deriveUrl"
                  method = FormMethod.get
                  table {
                    tr {
                      td { style = ""; label { +"GitHub Username or Org:" } }
                      td { textInput { name = ACCOUNT.asArg; size = "20" } }
                    }
                    tr {
                      td { style = ""; label { +"Repo Name:" } }
                      td { textInput { name = REPO.asArg; size = "20" } }
                    }
                    tr {
                      td { style = ""; label { +"Git Branch:" } }
                      td { textInput { name = BRANCH.asArg; size = "20"; value = "master" } }
                    }
                    tr {
                      td { style = ""; label { +"File Path:" } }
                      td { textInput { name = PATH.asArg; size = "70"; value = "/src/main/kotlin/..." } }
                    }
                    tr {
                      td { style = ""; label { +"Match Expr:" } }
                      td { textInput { name = REGEX.asArg; size = "20" } }
                    }
                    tr {
                      td { style = ""; label { +"Offset:" } }
                      td { textInput { name = OFFSET.asArg; size = "10"; value = "0" } }
                    }
                    tr {
                      td { style = ""; label { +"Occurence:" } }
                      td {
                        select {
                          name = OCCURENCE.asArg
                          size = "1"
                          option { +" 1st "; value = "1" }
                          option { +" 2nd "; value = "2" }
                          option { +" 3rd "; value = "3" }
                          option { +" 4th "; value = "4" }
                          option { +" 5th "; value = "5" }
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
                            name = DIRECTION.asArg
                            value = "true"
                            checked = true
                          }
                          label {
                            htmlFor = "topdown"
                            +" Top Down "
                          }
                          radioInput {
                            id = "bottomup"
                            name = DIRECTION.asArg
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
            Args
              .values()
              .map { it.asArg }
              .forEach { arg ->
                it[arg] = call.request.queryParameters[arg] ?: ""
              }
          }

      get("deriveUrl") {
        respondWith {
          document {
            append.html {
              body {
                val args = getVals().map { (k, v) -> "$k=${v.encode()}" }.joinToString("&")
                val url = "http://localhost:8080/src?$args"
                a { href = url; target = "_blank"; +"Test URL" }
              }
            }
          }.serialize()
        }
      }

      get("src") {
        val vals = getVals().apply { forEach { (k, v) -> println("$k=$v") } }

        val url = githubRawUrl(
          vals[ACCOUNT.asArg] ?: "",
          vals[REPO.asArg] ?: "",
          vals[PATH.asArg] ?: "",
          vals[BRANCH.asArg] ?: ""
        )
        val topDown = vals[DIRECTION.asArg]?.toBoolean() ?: true
        val lines = URL(url).readText().lines().let { if (topDown) it else it.asReversed() }

        val account = vals[ACCOUNT.asArg] ?: ""
        val repo = vals[REPO.asArg] ?: ""
        val path = vals[PATH.asArg] ?: ""
        val branch = vals[BRANCH.asArg] ?: ""
        val regex = Regex(vals[REGEX.asArg] ?: "")
        val occurence = vals[OCCURENCE.asArg]?.toInt() ?: 1
        val offset = vals[OFFSET.asArg]?.toInt() ?: 0

        val linenum =
          (lines
            .asSequence()
            .mapIndexed { index, s -> index to s.contains(regex) }
            .filter { it.second }
            .take(occurence)
            .first().first) + offset + 1

        redirectTo { githubSourceUrl(account, repo, path, branch, linenum) }
      }
    }
  }.start(wait = true)
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
