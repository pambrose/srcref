/*
 *   Copyright © 2026 Paul Ambrose (pambrose@mac.com)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.pambrose.srcref

import com.pambrose.common.util.encode
import com.pambrose.common.util.isNotNull
import com.pambrose.srcref.ContentCache.Companion.fetchContent
import com.pambrose.srcref.Endpoints.GITHUB
import com.pambrose.srcref.Endpoints.PROBLEM
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
import com.pambrose.srcref.Urls.calcLineNumber
import com.pambrose.srcref.pages.Common.hasValues
import java.util.regex.PatternSyntaxException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue
import org.apache.commons.text.StringEscapeUtils.escapeHtml4

/**
 * Core URL construction and line-number resolution logic for srcref.
 *
 * Orchestrates the full flow: validates parameters, fetches file content via [ContentCache],
 * runs regex matching with [calcLineNumber], and builds the final GitHub permalink.
 */
object Urls {
  internal const val MSG = "msg"
  internal const val RAW_PREFIX = "https://raw.githubusercontent.com"
  private val REGEX_TIMEOUT_NANOS = 5.seconds.inWholeNanoseconds

  /**
   * Converts this parameter map to a URL-encoded query string.
   *
   * @param ignoreEndParams if `true`, omits optional end-range parameters from the output.
   */
  internal fun Map<String, String?>.toQueryParams(ignoreEndParams: Boolean) =
    filter { if (ignoreEndParams) it.key !in QueryParams.optionalParams else true }
      .map { (k, v) -> if (v.isNotNull()) "$k=${v.encode()}" else "" }
      .filter { it.isNotBlank() }
      .joinToString("&")

  private fun Map<String, String?>.missingEndRegex() = this[END_REGEX.arg]?.isBlank() ?: true

  /**
   * Builds a srcref redirect URL from the given [params], optionally HTML-escaping the result.
   */
  internal fun srcrefToGithubUrl(
    params: Map<String, String?>,
    escapeHtml4: Boolean = false,
    prefix: String,
  ) = "$prefix/$GITHUB?${params.toQueryParams(params.missingEndRegex())}"
    .let { if (escapeHtml4) escapeHtml4(it) else it }

  /**
   * Parses this nullable string as an [Int], throwing [IllegalArgumentException] with
   * the message from [block] if the string is null or not a valid integer.
   */
  internal inline fun String?.toInt(block: () -> String) =
    try {
      this?.toInt() ?: throw IllegalArgumentException(block())
    } catch (e: NumberFormatException) {
      throw IllegalArgumentException(block())
    }

  /**
   * Resolves query [params] into a GitHub permalink URL with computed line numbers.
   *
   * Fetches the target file, runs regex matching for begin (and optionally end) lines,
   * and constructs the final `github.com/.../blob/...#L{begin}[-L{end}]` URL.
   *
   * @return a [Pair] of (URL, errorMessage). On success the error message is empty;
   *   on failure the URL points to the `/problem` page and the error message describes the issue.
   */
  internal suspend fun githubRangeUrl(
    params: Map<String, String?>,
    prefix: String,
  ): Pair<String, String> =
    runCatching {
      if (!params.hasValues()) {
        "" to ""
      } else {
        val account = ACCOUNT.required(params)
        val repo = REPO.required(params)
        val path = PATH.required(params).let { if (it.startsWith("/")) it.substring(1) else it }
        val branch = BRANCH.required(params)

        val url = githubRawUrl(account, repo, path, branch)
        val (lines, duration) = measureTimedValue { fetchContent(url) }
        logger.info { "Read ${url.removePrefix(RAW_PREFIX)} in $duration" }

        val beginOffsetStr = BEGIN_OFFSET.defaultIfNull(params)
        val beginOffset = beginOffsetStr.toInt { "Invalid Begin Offset value: $beginOffsetStr" }
        val beginLinenum =
          calcLineNumber(
            lines = lines,
            pattern = BEGIN_REGEX.required(params),
            occurrence = BEGIN_OCCURRENCE.defaultIfNull(params).toInt(),
            offset = beginOffset,
            topDown = BEGIN_TOPDOWN.defaultIfNull(params).toBoolean(),
            desc = "begin",
          ).also { if (it < 1) throw IllegalArgumentException("Begin line number is less than 1") }

        val endLinenum =
          if (END_REGEX.defaultIfNull(params).isBlank()) {
            -1
          } else {
            val endOffsetStr = END_OFFSET.defaultIfNull(params)
            val endOffset = endOffsetStr.toInt { "Invalid End Offset value: $endOffsetStr" }
            calcLineNumber(
              lines = lines,
              pattern = END_REGEX.defaultIfNull(params),
              occurrence = END_OCCURRENCE.defaultIfNull(params).toInt(),
              offset = endOffset,
              topDown = END_TOPDOWN.defaultIfNull(params).toBoolean(),
              desc = "end",
            ).also { if (it < 1) throw IllegalArgumentException("End line number is less than 1") }
          }
        githubSourceUrl(account, repo, branch, path, beginLinenum, endLinenum) to ""
      }
    }.getOrElse { e ->
      val msg = "${e::class.simpleName}: ${e.message}"
      logger.info { "Input problem: $msg $params" }
      "$prefix/$PROBLEM?$MSG=${msg.encode()}&${params.toQueryParams(false)}" to msg
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

  private fun githubRawUrl(
    username: String,
    repoName: String,
    path: String = "",
    branchName: String,
  ) = "$RAW_PREFIX/$username/$repoName/$branchName/$path"

  /**
   * Searches [lines] for the [occurrence]-th match of [pattern], applying [offset] to the result.
   *
   * The search proceeds top-down or bottom-up depending on [topDown]. A 5-second timeout
   * guards against catastrophic regex backtracking.
   *
   * @param lines the file content as a list of lines.
   * @param pattern the regex pattern to match against each line.
   * @param occurrence which match to select (1-based).
   * @param offset number of lines to add to the matched line number.
   * @param topDown `true` to search from the first line, `false` to search from the last.
   * @param desc label used in error messages (e.g., "begin" or "end").
   * @return the 1-based line number after applying the offset.
   * @throws IllegalArgumentException on invalid regex, timeout, or insufficient matches.
   */
  internal fun calcLineNumber(
    lines: List<String>,
    pattern: String,
    occurrence: Int,
    offset: Int,
    topDown: Boolean,
    desc: String = "",
  ): Int =
    runCatching {
      require(occurrence >= 1) { "Occurrence must be >= 1, got: $occurrence" }
      val regex = Regex(pattern)
      val deadline = System.nanoTime() + REGEX_TIMEOUT_NANOS
      (
        (if (topDown) lines else lines.asReversed())
          .asSequence()
          .mapIndexed { index, s ->
            if (System.nanoTime() > deadline) {
              throw IllegalArgumentException("Regex matching timed out for $desc regex: \"$pattern\"")
            }
            (if (topDown) index else (lines.size - index - 1)) to s.contains(regex)
          }
          .filter { it.second }
          .drop(occurrence - 1)
          .first().first
        ) + offset + 1
    }.getOrElse { e ->
      throw when (e) {
        is PatternSyntaxException -> {
          IllegalArgumentException("Invalid regex:\"${pattern}\" - ${e.message}")
        }

        is NoSuchElementException -> {
          IllegalArgumentException("Required matches ($occurrence) not found for $desc regex: \"$pattern\"")
        }

        else -> {
          logger.error { "Error in calcLineNumber(): ${e::class.simpleName}: ${e.message}" }
          e
        }
      }
    }
}
