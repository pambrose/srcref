package com.pambrose.srcref.pages

import kotlinx.html.HTMLTag
import kotlinx.html.unsafe

object Common {
  internal const val WIDTH_VAL = "93"

  internal val URL_PREFIX = (System.getenv("PREFIX") ?: "http://localhost:8080").removeSuffix("/")

  internal fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }

  internal fun Map<String, String?>.hasValues() = values.asSequence().filter { it?.isNotBlank() == true }.any()
}
