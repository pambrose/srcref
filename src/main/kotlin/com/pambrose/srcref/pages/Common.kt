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
