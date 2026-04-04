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

import com.pambrose.srcref.QueryParams.*
import com.pambrose.srcref.Urls.srcrefToGithubUrl

/**
 * Public API for programmatically generating srcref URLs.
 *
 * This object is intended for use as a library dependency (e.g., via Maven Central),
 * allowing consumers to generate srcref URLs from their own code.
 */
@Suppress("unused")
object Api {
  /**
   * Generates a srcref URL that, when visited, dynamically resolves to a GitHub permalink
   * with the appropriate line numbers based on the current file content.
   *
   * @param account GitHub username or organization name.
   * @param repo GitHub repository name.
   * @param path file path within the repository.
   * @param beginRegex regex pattern to match the beginning line.
   * @param beginOccurrence which occurrence of the begin regex to use (1-based).
   * @param beginOffset number of lines to offset from the begin match (positive = below, negative = above).
   * @param beginTopDown whether to search top-down (`true`) or bottom-up (`false`) for the begin match.
   * @param endRegex optional regex pattern to match the ending line. Empty string means no end line.
   * @param endOccurrence which occurrence of the end regex to use (1-based).
   * @param endOffset number of lines to offset from the end match.
   * @param endTopDown whether to search top-down or bottom-up for the end match.
   * @param prefix URL prefix for the srcref service.
   * @param branch GitHub branch name.
   * @param escapeHtml4 whether to HTML-escape the resulting URL for safe embedding in HTML.
   * @return the generated srcref URL string.
   */
  fun srcrefUrl(
    account: String,
    repo: String,
    path: String,
    beginRegex: String,
    beginOccurrence: Int = 1,
    beginOffset: Int = 0,
    beginTopDown: Boolean = true,
    endRegex: String = "",
    endOccurrence: Int = 1,
    endOffset: Int = 0,
    endTopDown: Boolean = true,
    prefix: String = "https://www.srcref.com",
    branch: String = "master",
    escapeHtml4: Boolean = false,
  ): String =
    srcrefToGithubUrl(
      mapOf(
        ACCOUNT.arg to account,
        REPO.arg to repo,
        BRANCH.arg to branch,
        PATH.arg to path,
        BEGIN_REGEX.arg to beginRegex,
        BEGIN_OCCURRENCE.arg to beginOccurrence.toString(),
        BEGIN_OFFSET.arg to beginOffset.toString(),
        BEGIN_TOPDOWN.arg to beginTopDown.toString(),
        END_REGEX.arg to endRegex,
        END_OCCURRENCE.arg to endOccurrence.toString(),
        END_OFFSET.arg to endOffset.toString(),
        END_TOPDOWN.arg to endTopDown.toString(),
      ),
      escapeHtml4,
      prefix,
    )
}
