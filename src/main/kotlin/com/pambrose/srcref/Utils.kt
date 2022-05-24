package com.pambrose.srcref

import com.github.pambrose.common.util.*
import com.pambrose.srcref.QueryArgs.ACCOUNT
import com.pambrose.srcref.QueryArgs.BRANCH
import com.pambrose.srcref.QueryArgs.OCCURRENCE
import com.pambrose.srcref.QueryArgs.OFFSET
import com.pambrose.srcref.QueryArgs.PATH
import com.pambrose.srcref.QueryArgs.REGEX
import com.pambrose.srcref.QueryArgs.REPO
import com.pambrose.srcref.QueryArgs.TOPDOWN
import mu.*
import java.net.*
import java.util.regex.*

object Utils : KLogging() {
  private val urlPrefix = (System.getenv("PREFIX") ?: "http://localhost:8080").removeSuffix("/")
  const val githubref = "githubRef"

  fun srcRefUrl(params: Map<String, String?>): String {
    val args =
      params
        .map { (k, v) -> if (v.isNotNull()) "$k=${v.encode()}" else "" }
        .filter { it.isNotBlank() }
        .joinToString("&")
    return "$urlPrefix/$githubref?$args"
  }

  fun srcRefUrl(
    account: String,
    repo: String,
    branch: String,
    path: String,
    regex: String,
    occurrence: Int,
    offset: Int,
    topDown: Boolean
  ) =
    srcRefUrl(
      mapOf(
        ACCOUNT.arg to account,
        REPO.arg to repo,
        BRANCH.arg to branch,
        PATH.arg to path,
        REGEX.arg to regex,
        OCCURRENCE.arg to occurrence.toString(),
        OFFSET.arg to offset.toString(),
        TOPDOWN.arg to topDown.toString()
      )
    )

  fun githubRefUrl(params: Map<String, String?>) =
    try {
      val account = ACCOUNT.required(params)
      val repo = REPO.required(params)
      val branch = BRANCH.required(params)
      val path = PATH.required(params)

      val url = githubRawUrl(account, repo, path, branch)

      val lines = URL(url).readText().lines()
      val linenum =
        calcLineNumber(
          lines,
          REGEX.required(params),
          OCCURRENCE.defaultIfNull(params).toInt(),
          OFFSET.defaultIfNull(params).toInt(),
          TOPDOWN.defaultIfNull(params).toBoolean()
        ).also { if (it < 1) throw IllegalArgumentException("Line number is less than 1") }
      githubSourceUrl(account, repo, path, branch, linenum)
    } catch (e: Throwable) {
      "${e::class.simpleName}: ${e.message}"
    }

  fun calcLineNumber(lines: List<String>, pattern: String, occurrence: Int, offset: Int, topDown: Boolean) =
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
      when (e) {
        is PatternSyntaxException -> throw IllegalArgumentException("Invalid regex:\"${pattern}\" - ${e.message}")
        is NoSuchElementException -> throw IllegalArgumentException("Requested matches ($occurrence) not found for regex: \"$pattern\"")
        else -> throw e
      }
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