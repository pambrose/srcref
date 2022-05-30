package com.pambrose.srcref

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
import com.pambrose.srcref.Urls.srcrefToGithubUrl

@Suppress("unused")
object Api {
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