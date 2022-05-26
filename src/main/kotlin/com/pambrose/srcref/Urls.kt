package com.pambrose.srcref

import com.github.pambrose.common.util.*
import com.pambrose.srcref.QueryArgs.ACCOUNT
import com.pambrose.srcref.QueryArgs.BEGIN_OCCURRENCE
import com.pambrose.srcref.QueryArgs.BEGIN_OFFSET
import com.pambrose.srcref.QueryArgs.BEGIN_REGEX
import com.pambrose.srcref.QueryArgs.BEGIN_TOPDOWN
import com.pambrose.srcref.QueryArgs.BRANCH
import com.pambrose.srcref.QueryArgs.END_OCCURRENCE
import com.pambrose.srcref.QueryArgs.END_OFFSET
import com.pambrose.srcref.QueryArgs.END_REGEX
import com.pambrose.srcref.QueryArgs.END_TOPDOWN
import com.pambrose.srcref.QueryArgs.PATH
import com.pambrose.srcref.QueryArgs.REPO
import com.pambrose.srcref.SrcRef.logger
import org.apache.commons.text.*
import java.net.*
import java.util.regex.*

object Urls {
  const val EDIT = "edit"
  const val GITHUB = "github"
  const val ERROR = "error"
  const val MSG = "msg"
  const val ARGS = "args"

  internal fun srcrefToGithubUrl(
    params: Map<String, String?>,
    escapeHtml4: Boolean = false,
    prefix: String = "https://www.srcref.com"
  ): String {
    val args =
      params
        .map { (k, v) -> if (v.isNotNull()) "$k=${v.encode()}" else "" }
        .filter { it.isNotBlank() }
        .joinToString("&")
    return "$prefix/$GITHUB?$args".let { if (escapeHtml4) StringEscapeUtils.escapeHtml4(it) else it }
  }

  internal fun githubRangeUrl(params: Map<String, String?>, prefix: String) =
    try {
      val account = ACCOUNT.required(params)
      val repo = REPO.required(params)
      val path = PATH.required(params).let { if (it.startsWith("/")) it.substring(1) else it }
      val branch = BRANCH.required(params)

      val url = githubRawUrl(account, repo, path, branch)

      val lines = URL(url).readText().lines()
      val beginLinenum =
        calcLineNumber(
          lines,
          BEGIN_REGEX.required(params),
          BEGIN_OCCURRENCE.defaultIfNull(params).toInt(),
          BEGIN_OFFSET.defaultIfNull(params).toInt(),
          BEGIN_TOPDOWN.defaultIfNull(params).toBoolean()
        ).also { if (it < 1) throw IllegalArgumentException("Begin line number is less than 1") }

      val endLinenum =
        if (END_REGEX.defaultIfNull(params).isBlank())
          -1
        else
          calcLineNumber(
            lines,
            END_REGEX.defaultIfNull(params),
            END_OCCURRENCE.defaultIfNull(params).toInt(),
            END_OFFSET.defaultIfNull(params).toInt(),
            END_TOPDOWN.defaultIfNull(params).toBoolean()
          ).also { if (it < 1) throw IllegalArgumentException("End line number is less than 1") }

      githubSourceUrl(account, repo, branch, path, beginLinenum, endLinenum)
    } catch (e: Throwable) {
      val msg = "${e::class.simpleName}: ${e.message}".encode()
      val args = params.toString().encode()
      "$prefix/$ERROR?$MSG=$msg&$ARGS=$args"
    }

  private fun githubSourceUrl(
    username: String,
    repoName: String,
    branchName: String,
    path: String = "",
    beginLineNum: Int,
    endLineNum: Int,
  ): String {
    val suffix = if (endLineNum > 0 && beginLineNum != endLineNum) "-L$endLineNum" else ""
    return "https://github.com/$username/$repoName/blob/$branchName/$path#L$beginLineNum$suffix"
  }

  private fun githubRawUrl(username: String, repoName: String, path: String = "", branchName: String = "master") =
    "https://raw.githubusercontent.com/$username/$repoName/$branchName/$path"

  internal fun calcLineNumber(lines: List<String>, pattern: String, occurrence: Int, offset: Int, topDown: Boolean) =
    try {
      val regex = Regex(pattern)
      ((if (topDown) lines else lines.asReversed())
        .asSequence()
        .mapIndexed { index, s -> (if (topDown) index else (lines.size - index - 1)) to s.contains(regex) }
        .filter { it.second }
        .drop(occurrence - 1)
        .first().first) + offset + 1
    } catch (e: Throwable) {
      when (e) {
        is PatternSyntaxException -> {
          val msg = "Invalid regex:\"${pattern}\" - ${e.message}"
          logger.info { "Problem in calcLineNumber(): $msg" }
          throw IllegalArgumentException(msg)
        }
        is NoSuchElementException -> {
          val msg = "Required matches ($occurrence) not found for regex: \"$pattern\""
          logger.info { "Problem in calcLineNumber(): $msg" }
          throw IllegalArgumentException(msg)
        }
        else -> {
          val msg = "${e::class.simpleName}: ${e.message}"
          logger.error { "Error in calcLineNumber(): $msg" }
          throw e
        }
      }
    }
}