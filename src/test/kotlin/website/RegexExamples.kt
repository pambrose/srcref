@file:Suppress("unused")

package website

import com.pambrose.srcref.Api.srcrefUrl

object RegexExamples {

  // --8<-- [start:literal-string]
  // Match a literal string — no special regex characters needed
  val literalMatch =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "object Main",
    )
  // --8<-- [end:literal-string]

  // --8<-- [start:escape-parens]
  // Parentheses are regex metacharacters — escape them with backslashes
  val escapedParens =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "install\\(CallLogging\\)",
    )
  // --8<-- [end:escape-parens]

  // --8<-- [start:escape-brackets]
  // Square brackets define character classes — escape them for literals
  val escapedBrackets =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/QueryParams.kt",
      beginRegex = "filter \\{ it\\.optional \\}",
    )
  // --8<-- [end:escape-brackets]

  // --8<-- [start:escape-braces]
  // Curly braces are quantifiers in regex — escape them for literals
  val escapedBraces =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Routes.kt",
      beginRegex = "buildMap \\{",
    )
  // --8<-- [end:escape-braces]

  // --8<-- [start:dot-wildcard]
  // A dot matches any character — useful for flexible matching
  val dotWildcard =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "install\\(.+\\)",
    )
  // --8<-- [end:dot-wildcard]

  // --8<-- [start:word-boundary]
  // Use \b for word boundaries to avoid partial matches
  val wordBoundary =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "\\bfun\\b.*calcLineNumber",
    )
  // --8<-- [end:word-boundary]

  // --8<-- [start:anchor-start]
  // Use ^ to match only at the start of a line
  val anchorStart =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "^import",
    )
  // --8<-- [end:anchor-start]

  // --8<-- [start:alternation]
  // Use | for alternation to match one of several patterns
  val alternation =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "install\\(Compression\\)|install\\(DefaultHeaders\\)",
    )
  // --8<-- [end:alternation]

  // --8<-- [start:character-class]
  // Use character classes for flexible single-character matching
  val characterClass =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "\\bval\\s+[a-z]\\w*Str\\b",
    )
  // --8<-- [end:character-class]

  // --8<-- [start:quantifiers]
  // Common quantifiers: + (one or more), * (zero or more), ? (optional)
  val quantifiers =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "fun\\s+\\w+\\(",
    )
  // --8<-- [end:quantifiers]

  // --8<-- [start:function-pattern]
  // Match any function declaration
  val functionPattern =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "\\bfun\\s+\\w+\\(",
    )
  // --8<-- [end:function-pattern]

  // --8<-- [start:class-pattern]
  // Match a class or object declaration
  val classPattern =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/ContentCache.kt",
      beginRegex = "(class|object)\\s+ContentCache",
    )
  // --8<-- [end:class-pattern]

  // --8<-- [start:enum-entry-pattern]
  // Match a specific enum entry
  val enumEntry =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/QueryParams.kt",
      beginRegex = "BEGIN_REGEX\\(",
    )
  // --8<-- [end:enum-entry-pattern]

  // --8<-- [start:comment-pattern]
  // Match a comment containing specific text
  val commentPattern =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Urls.kt",
      beginRegex = "//.*5.second",
    )
  // --8<-- [end:comment-pattern]

  // --8<-- [start:annotation-pattern]
  // Match an annotation
  val annotationPattern =
    srcrefUrl(
      account = "pambrose",
      repo = "srcref",
      path = "src/main/kotlin/com/pambrose/srcref/Main.kt",
      beginRegex = "@Version\\(",
    )
  // --8<-- [end:annotation-pattern]
}
