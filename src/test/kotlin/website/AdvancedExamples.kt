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

@file:Suppress("unused")

package website

import com.pambrose.srcref.Api.srcrefUrl

object AdvancedExamples {
  // --8<-- [start:bottom-up-last-brace]
  // Find the last closing brace in a file (useful for end of class/object)
  val lastBrace =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "object Urls",
      endRegex = "^\\}",
      endTopDown = false,
    )
  // --8<-- [end:bottom-up-last-brace]

  // --8<-- [start:entire-function]
  // Highlight an entire function from declaration to closing brace
  val entireFunction =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "internal fun calcLineNumber",
      endRegex = "^\\ \\ \\}",
      endOccurrence = 1,
      endTopDown = false,
    )
  // --8<-- [end:entire-function]

  // --8<-- [start:skip-to-body]
  // Skip past the function signature to highlight only the body
  val functionBody =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "internal fun calcLineNumber",
      beginOffset = 1,
      endRegex = "^\\ \\ \\}",
      endOccurrence = 1,
      endTopDown = false,
      endOffset = -1,
    )
  // --8<-- [end:skip-to-body]

  // --8<-- [start:negative-offset]
  // Use negative offsets to highlight lines before the match
  val contextAbove =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "internal fun calcLineNumber",
      beginOffset = -5,
    )
  // --8<-- [end:negative-offset]

  // --8<-- [start:nth-occurrence]
  // Match the 3rd occurrence of "install(" in Main.kt
  val thirdInstall =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "install\\(",
      beginOccurrence = 3,
    )
  // --8<-- [end:nth-occurrence]

  // --8<-- [start:companion-object]
  // Highlight a companion object block
  val companionBlock =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/ContentCache.kt",
      beginRegex = "companion object",
      endRegex = "^\\ \\ \\}",
      endTopDown = false,
    )
  // --8<-- [end:companion-object]

  // --8<-- [start:when-expression]
  // Highlight a when expression block
  val whenBlock =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/ContentCache.kt",
      beginRegex = "return when",
      endRegex = "^\\ \\ \\ \\ \\ \\ \\}",
      endTopDown = false,
    )
  // --8<-- [end:when-expression]

  // --8<-- [start:enum-values]
  // Highlight all enum entries in QueryParams
  val enumEntries =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/QueryParams.kt",
      beginRegex = "ACCOUNT\\(",
      endRegex = "END_TOPDOWN\\(",
      endOffset = 0,
    )
  // --8<-- [end:enum-values]

  // --8<-- [start:documentation-link]
  // Generate a link for use in Markdown documentation
  val markdownLink =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Api.kt",
      beginRegex = "fun srcrefUrl\\(",
      endRegex = "^\\ \\ \\)",
      endTopDown = false,
    )
  // In Markdown: [View the srcrefUrl() function]($markdownLink)
  // --8<-- [end:documentation-link]

  // --8<-- [start:html-link]
  // Generate an HTML-escaped link for embedding in HTML pages
  val htmlLink =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Api.kt",
      beginRegex = "fun srcrefUrl\\(",
      escapeHtml4 = true,
    )
  // In HTML: <a href="$htmlLink">View source</a>
  // --8<-- [end:html-link]

  // --8<-- [start:route-handler]
  // Highlight a specific route handler
  val routeHandler =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Routes.kt",
      beginRegex = "get\\(GITHUB\\.path\\)",
      endRegex = "^\\s+\\}",
      endOccurrence = 1,
    )
  // --8<-- [end:route-handler]

  // --8<-- [start:multiple-files]
  // You can create srcref URLs pointing to different files in the same repo
  val mainFile =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "fun main",
    )

  val routesFile =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Routes.kt",
      beginRegex = "fun Application\\.configureRoutes",
    )

  val urlsFile =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "object Urls",
    )
  // --8<-- [end:multiple-files]
}
