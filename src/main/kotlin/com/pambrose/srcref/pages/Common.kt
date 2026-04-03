package com.pambrose.srcref.pages

import kotlinx.html.HTMLTag
import kotlinx.html.unsafe

/** Shared constants and utility functions used across all HTML pages. */
object Common {
  internal const val WIDTH_VAL = "93"

  internal val URL_PREFIX = (System.getenv("PREFIX") ?: "http://localhost:8080").removeSuffix("/")

  /** Inserts raw, unescaped [html] into the current element. */
  internal fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }

  /** Returns `true` if this parameter map contains at least one non-blank value. */
  internal fun Map<String, String?>.hasValues() = values.asSequence().filter { it?.isNotBlank() == true }.any()
}
