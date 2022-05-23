import QueryArgs.ACCOUNT
import QueryArgs.BRANCH
import QueryArgs.OCCURRENCE
import QueryArgs.OFFSET
import QueryArgs.PATH
import QueryArgs.REGEX
import QueryArgs.REPO
import QueryArgs.TOPDOWN
import mu.*
import java.net.*

object Utils : KLogging() {
  fun githubRefUrl(params: Map<String, String?>) =
    try {
      val account = ACCOUNT.required(params)
      val repo = REPO.required(params)
      val path = PATH.required(params)
      val branch = BRANCH.required(params)
      val url = githubRawUrl(account, repo, path, branch)

      val lines = URL(url).readText().lines()
      val linenum =
        calcLineNumber(
          lines,
          TOPDOWN.defaultIfNull(params).toBoolean(),
          REGEX.required(params),
          OCCURRENCE.defaultIfNull(params).toInt(),
          OFFSET.defaultIfNull(params).toInt()
        )
      githubSourceUrl(account, repo, path, branch, linenum)
    } catch (e: Throwable) {
      "Invalid inputs: ${e.message}"
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