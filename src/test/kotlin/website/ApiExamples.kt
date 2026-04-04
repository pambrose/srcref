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

object ApiExamples {
  // --8<-- [start:basic-usage]
  val url =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "fun main",
    )
  // --8<-- [end:basic-usage]

  // --8<-- [start:single-line]
  val singleLine =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "fun main",
    )
  // Use the URL in documentation:
  // <a href="$singleLine">View the main function</a>
  // --8<-- [end:single-line]

  // --8<-- [start:line-range]
  val lineRange =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "install\\(CallLogging\\)",
      endRegex = "install\\(Compression\\)",
      endOffset = 3,
    )
  // --8<-- [end:line-range]

  // --8<-- [start:occurrence]
  val secondOccurrence =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "install\\(",
      beginOccurrence = 2,
    )
  // --8<-- [end:occurrence]

  // --8<-- [start:bottom-up]
  val bottomUp =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "install\\(",
      beginTopDown = false,
    )
  // --8<-- [end:bottom-up]

  // --8<-- [start:offset]
  val withOffset =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "install\\(CallLogging\\)",
      beginOffset = -1,
      endRegex = "install\\(Compression\\)",
      endOffset = 5,
    )
  // --8<-- [end:offset]

  // --8<-- [start:html-escaped]
  val htmlSafe =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "fun main",
      escapeHtml4 = true,
    )
  // Safe to embed directly in HTML: <a href="$htmlSafe">link</a>
  // --8<-- [end:html-escaped]

  // --8<-- [start:custom-prefix]
  val selfHosted =
    srcrefUrl(
      account = "myorg",
      repo = "myrepo",
      path = "src/App.kt",
      beginRegex = "class App",
      prefix = "https://srcref.internal.myorg.com",
      branch = "develop",
    )
  // --8<-- [end:custom-prefix]

  // --8<-- [start:custom-branch]
  val fromDevelop =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "fun main",
      branch = "develop",
    )
  // --8<-- [end:custom-branch]

  // --8<-- [start:complex-range]
  val complexRange =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "fun Application\\.module\\(\\)",
      beginOffset = 0,
      endRegex = "configureRoutes\\(\\)",
      endOccurrence = 1,
      endOffset = 1,
      endTopDown = true,
    )
  // --8<-- [end:complex-range]

  // --8<-- [start:class-definition]
  val classLink =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/ContentCache.kt",
      beginRegex = "internal class ContentCache",
      endRegex = "^\\}",
      endTopDown = false,
    )
  // --8<-- [end:class-definition]

  // --8<-- [start:function-highlight]
  val functionLink =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "internal fun calcLineNumber",
      endRegex = "^\\ \\ \\}",
      endTopDown = false,
    )
  // --8<-- [end:function-highlight]

  // --8<-- [start:import-statement]
  val importLink =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "^import",
      beginOccurrence = 1,
      endRegex = "^import",
      endTopDown = false,
    )
  // --8<-- [end:import-statement]
}
