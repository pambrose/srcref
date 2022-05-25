package com.pambrose.srcref

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
import com.pambrose.srcref.Urls.srcrefToGithubUrl

@Suppress("unused")
object Api {
  fun srcrefUrl(
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
    srcrefToGithubUrl(
      mapOf(
        ACCOUNT.arg to account,
        REPO.arg to repo,
        BRANCH.arg to branch,
        PATH.arg to path,
        BEGIN_REGEX.arg to begin_regex,
        BEGIN_OCCURRENCE.arg to begin_occurrence.toString(),
        BEGIN_OFFSET.arg to begin_offset.toString(),
        BEGIN_TOPDOWN.arg to begin_topDown.toString(),
        END_REGEX.arg to end_regex,
        END_OCCURRENCE.arg to end_occurrence.toString(),
        END_OFFSET.arg to end_offset.toString(),
        END_TOPDOWN.arg to end_topDown.toString(),
      ),
      escapeHtml4,
      prefix,
    )
}