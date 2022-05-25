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
import kotlinx.html.*
import org.apache.commons.text.*
import java.net.*
import java.util.regex.*

object Utils {
  const val editRef = "edit"
  const val githubRef = "github"
  const val errorRef = "error"

  fun srcRefUrl(
    params: Map<String, String?>,
    escapeHtml4: Boolean = false,
    prefix: String = "https://www.srcref.com"
  ): String {
    val args =
      params
        .map { (k, v) -> if (v.isNotNull()) "$k=${v.encode()}" else "" }
        .filter { it.isNotBlank() }
        .joinToString("&")
    return "$prefix/$githubRef?$args".let { if (escapeHtml4) StringEscapeUtils.escapeHtml4(it) else it }
  }

  fun srcRefUrl(
    account: String,
    repo: String,
    path: String,
    begin_regex: String,
    begin_occurrence: Int = 1,
    begin_offset: Int = 0,
    begin_topDown: Boolean = true,
    end_regex: String = "",
    end_occurrence: Int = 1,
    end_offset: Int = 0,
    end_topDown: Boolean = true,
    prefix: String = "https://www.srcref.com",
    branch: String = "master",
    escapeHtml4: Boolean = false,
  ) =
    srcRefUrl(
      mapOf(
        ACCOUNT.arg to account,
        REPO.arg to repo,
        PATH.arg to path,
        BEGIN_REGEX.arg to begin_regex,
        BEGIN_OCCURRENCE.arg to begin_occurrence.toString(),
        BEGIN_OFFSET.arg to begin_offset.toString(),
        BEGIN_TOPDOWN.arg to begin_topDown.toString(),
        END_REGEX.arg to end_regex,
        END_OCCURRENCE.arg to end_occurrence.toString(),
        END_OFFSET.arg to end_offset.toString(),
        END_TOPDOWN.arg to end_topDown.toString(),
        BRANCH.arg to branch,
      ),
      escapeHtml4,
      prefix,
    )

  fun githubRefUrl(params: Map<String, String?>, prefix: String) =
    try {
      val account = ACCOUNT.required(params)
      val repo = REPO.required(params)
      val path = PATH.required(params)
      val branch = BRANCH.required(params)

      val url = githubRawUrl(account, repo, path, branch)

      val lines = URL(url).readText().lines()
      val begin_linenum =
        calcLineNumber(
          lines,
          BEGIN_REGEX.required(params),
          BEGIN_OCCURRENCE.defaultIfNull(params).toInt(),
          BEGIN_OFFSET.defaultIfNull(params).toInt(),
          BEGIN_TOPDOWN.defaultIfNull(params).toBoolean()
        ).also { if (it < 1) throw IllegalArgumentException("Begin line number is less than 1") }

      val end_linenum =
        if (END_REGEX.defaultIfNull(params).isBlank()) {
          -1
        } else
          calcLineNumber(
            lines,
            END_REGEX.defaultIfNull(params),
            END_OCCURRENCE.defaultIfNull(params).toInt(),
            END_OFFSET.defaultIfNull(params).toInt(),
            END_TOPDOWN.defaultIfNull(params).toBoolean()
          ).also { if (it < 1) throw IllegalArgumentException("End line number is less than 1") }

      githubSourceUrl(account, repo, branch, path, begin_linenum, end_linenum)
    } catch (e: Throwable) {
      val msg = "${e::class.simpleName}: ${e.message}".encode()
      val args = params.toString().encode()
      "$prefix/$errorRef?msg=$msg&args=$args"
    }

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
      logger.info(e) { "Error in calcLineNumber()" }
      when (e) {
        is PatternSyntaxException -> throw IllegalArgumentException("Invalid regex:\"${pattern}\" - ${e.message}")
        is NoSuchElementException -> throw IllegalArgumentException("Requested matches ($occurrence) not found for regex: \"$pattern\"")
        else -> throw e
      }
    }

  fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }

  private fun githubSourceUrl(
    username: String,
    repoName: String,
    branchName: String,
    path: String = "",
    begin_lineNum: Int,
    end_lineNum: Int,
  ) =
    "https://github.com/$username/$repoName/blob/$branchName/$path#L$begin_lineNum${if (end_lineNum > 0) "-L$end_lineNum" else ""}"

  private fun githubRawUrl(username: String, repoName: String, path: String = "", branchName: String = "master") =
    "https://raw.githubusercontent.com/$username/$repoName/$branchName/$path"
}