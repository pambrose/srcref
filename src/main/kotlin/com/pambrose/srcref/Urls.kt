package com.pambrose.srcref

import com.github.pambrose.common.util.encode
import com.github.pambrose.common.util.isNotNull
import com.pambrose.srcref.ContentCache.Companion.fetchContent
import com.pambrose.srcref.Endpoints.EXCEPTION
import com.pambrose.srcref.Endpoints.GITHUB
import com.pambrose.srcref.Main.logger
import com.pambrose.srcref.QueryParams.ACCOUNT
import com.pambrose.srcref.QueryParams.BEGIN_OCCURRENCE
import com.pambrose.srcref.QueryParams.BEGIN_OFFSET
import com.pambrose.srcref.QueryParams.BEGIN_REGEX
import com.pambrose.srcref.QueryParams.BEGIN_TOPDOWN
import com.pambrose.srcref.QueryParams.BRANCH
import com.pambrose.srcref.QueryParams.END_OCCURRENCE
import com.pambrose.srcref.QueryParams.END_OFFSET
import com.pambrose.srcref.QueryParams.END_REGEX
import com.pambrose.srcref.QueryParams.END_TOPDOWN
import com.pambrose.srcref.QueryParams.PATH
import com.pambrose.srcref.QueryParams.REPO
import com.pambrose.srcref.pages.Common.hasValues
import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import java.util.regex.PatternSyntaxException
import kotlin.time.measureTimedValue

object Urls {
  internal const val MSG = "msg"
  internal const val RAW_PREFIX = "https://raw.githubusercontent.com"

  internal fun Map<String, String?>.toQueryParams(ignoreEndParams: Boolean) =
    filter { if (ignoreEndParams) it.key !in QueryParams.optionalParams else true }
      .map { (k, v) -> if (v.isNotNull()) "$k=${v.encode()}" else "" }
      .filter { it.isNotBlank() }
      .joinToString("&")

  private fun Map<String, String?>.missingEndRegex() = this[END_REGEX.arg]?.isBlank() ?: true

  internal fun srcrefToGithubUrl(
    params: Map<String, String?>,
    escapeHtml4: Boolean = false,
    prefix: String,
  ) =
    "$prefix/$GITHUB?${params.toQueryParams(params.missingEndRegex())}"
      .let { if (escapeHtml4) escapeHtml4(it) else it }

  internal inline fun String?.toInt(block: () -> String) =
    try {
      this?.toInt() ?: throw IllegalArgumentException(block())
    } catch (e: NumberFormatException) {
      throw IllegalArgumentException(block())
    }

  // This returns a url and an error message
  internal suspend fun githubRangeUrl(params: Map<String, String?>, prefix: String): Pair<String, String> =
    try {
      if (!params.hasValues()) {
        "" to ""
      } else {
        val account = ACCOUNT.required(params)
        val repo = REPO.required(params)
        val path = PATH.required(params).let { if (it.startsWith("/")) it.substring(1) else it }
        val branch = BRANCH.required(params)

        val url = githubRawUrl(account, repo, path, branch)
        val (lines, duration) = measureTimedValue { fetchContent(url) }
        logger.info("Read ${url.removePrefix(RAW_PREFIX)} in $duration")

        val beginOffsetStr = BEGIN_OFFSET.defaultIfNull(params)
        val beginOffset = beginOffsetStr.toInt { "Invalid Begin Offset value: $beginOffsetStr" }
        val beginLinenum =
          calcLineNumber(
            lines,
            BEGIN_REGEX.required(params),
            BEGIN_OCCURRENCE.defaultIfNull(params).toInt(),
            beginOffset,
            BEGIN_TOPDOWN.defaultIfNull(params).toBoolean(),
            "begin",
          ).also { if (it < 1) throw IllegalArgumentException("Begin line number is less than 1") }

        val endLinenum =
          if (END_REGEX.defaultIfNull(params).isBlank()) {
            -1
          } else {
            val endOffsetStr = END_OFFSET.defaultIfNull(params)
            val endOffset = endOffsetStr.toInt { "Invalid End Offset value: $endOffsetStr" }
            calcLineNumber(
              lines,
              END_REGEX.defaultIfNull(params),
              END_OCCURRENCE.defaultIfNull(params).toInt(),
              endOffset,
              END_TOPDOWN.defaultIfNull(params).toBoolean(),
              "end",
            ).also { if (it < 1) throw IllegalArgumentException("End line number is less than 1") }
          }
        githubSourceUrl(account, repo, branch, path, beginLinenum, endLinenum) to ""
      }
    } catch (e: Throwable) {
      val msg = "${e::class.simpleName}: ${e.message}"
      logger.info { "Input problem: $msg $params" }
      "$prefix/$EXCEPTION?$MSG=${msg.encode()}&${params.toQueryParams(false)}" to msg
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

  private fun githubRawUrl(username: String, repoName: String, path: String = "", branchName: String) =
    "$RAW_PREFIX/$username/$repoName/$branchName/$path"

  internal fun calcLineNumber(
    lines: List<String>,
    pattern: String,
    occurrence: Int,
    offset: Int,
    topDown: Boolean,
    desc: String = ""
  ) =
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
        is PatternSyntaxException ->
          throw IllegalArgumentException("Invalid regex:\"${pattern}\" - ${e.message}")
        is NoSuchElementException ->
          throw IllegalArgumentException("Required matches ($occurrence) not found for $desc regex: \"$pattern\"")
        else -> {
          logger.error { "Error in calcLineNumber(): ${e::class.simpleName}: ${e.message}" }
          throw e
        }
      }
    }
}