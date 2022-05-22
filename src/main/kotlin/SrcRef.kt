import QueryArgs.ACCOUNT
import QueryArgs.BRANCH
import QueryArgs.OCCURRENCE
import QueryArgs.OFFSET
import QueryArgs.PATH
import QueryArgs.REGEX
import QueryArgs.REPO
import QueryArgs.TOPDOWN
import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import mu.*
import java.net.*
import kotlin.collections.set

object SrcRef : KLogging() {
  const val githubref = "githubRef"
  val urlPrefix = (System.getenv("PREFIX") ?: "http://localhost:8080").removeSuffix("/")

  val PipelineContext<Unit, ApplicationCall>.queryParams
    get() =
      mutableMapOf<String, String>()
        .also {
          QueryArgs
            .values()
            .map { it.arg }
            .forEach { arg ->
              it[arg] = call.request.queryParameters[arg] ?: ""
            }
        }

  fun githubRefUrl(params: Map<String, String>) =
    try {
      val account = params[ACCOUNT.arg] ?: throw IllegalArgumentException()
      val repo = params[REPO.arg] ?: throw IllegalArgumentException()
      val path = params[PATH.arg] ?: throw IllegalArgumentException()
      val branch = params[BRANCH.arg] ?: throw IllegalArgumentException()
      val url = githubRawUrl(account, repo, path, branch)
      val lines = URL(url).readText().lines()
      val linenum =
        calcLineNumber(
          lines,
          params[TOPDOWN.arg]?.toBoolean() ?: true,
          params[REGEX.arg] ?: "",
          params[OCCURRENCE.arg]?.toInt() ?: 1,
          params[OFFSET.arg]?.toInt() ?: 0
        )
      githubSourceUrl(account, repo, path, branch, linenum)
    } catch (e: Throwable) {
      "Invalid"
    }

  fun calcLineNumber(lines: List<String>, topDown: Boolean, pattern: String, occurrence: Int, offset: Int) =
    try {
      val regex = Regex(pattern)
      ((if (topDown) lines else lines.asReversed())
        .asSequence()
        .mapIndexed { index, s -> (if (topDown) index else (lines.size - index - 1)) to s.contains(regex) }
        .filter { it.second }
        .drop(occurrence - 1)
        .first().first) + offset + 1
    } catch (e: Throwable) {
      logger.info(e) { "Error in calcLineNumber()" }
      1
    }

  private fun githubSourceUrl(
    username: String,
    repoName: String,
    path: String = "",
    branchName: String = "master",
    lineNum: Int
  ) =
    "https://github.com/$username/$repoName/blob/$branchName/$path#L$lineNum"

  private fun githubRawUrl(username: String, repoName: String, path: String = "", branchName: String = "master") =
    "https://raw.githubusercontent.com/$username/$repoName/$branchName/$path"
}

fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }

enum class QueryArgs {
  ACCOUNT, REPO, BRANCH, PATH, REGEX, OFFSET, OCCURRENCE, TOPDOWN;

  val arg get() = name.lowercase()
}

