import com.pambrose.srcref.Api.srcrefUrl
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith

class ApiTest :
  StringSpec(
    {
      "srcrefUrl with default parameters" {
        val url =
          srcrefUrl(
            account = "user",
            repo = "repo",
            path = "src/Main.kt",
            beginRegex = "fun main",
          )
        url shouldStartWith "https://www.srcref.com/github?"
        url shouldContain "account=user"
        url shouldContain "repo=repo"
        url shouldContain "branch=master"
        url shouldContain "bregex="
      }

      "srcrefUrl with custom begin parameters" {
        val url =
          srcrefUrl(
            account = "user",
            repo = "repo",
            path = "file.kt",
            beginRegex = "pattern",
            beginOccurrence = 2,
            beginOffset = 3,
            beginTopDown = false,
          )
        url shouldContain "boccur=2"
        url shouldContain "boffset=3"
        url shouldContain "btopd=false"
      }

      "srcrefUrl includes end params when endRegex is non-empty" {
        val url =
          srcrefUrl(
            account = "user",
            repo = "repo",
            path = "file.kt",
            beginRegex = "begin",
            endRegex = "end",
            endOccurrence = 2,
            endOffset = 1,
            endTopDown = false,
          )
        url shouldContain "eregex="
        url shouldContain "eoccur=2"
        url shouldContain "eoffset=1"
        url shouldContain "etopd=false"
      }

      "srcrefUrl omits end params when endRegex is empty" {
        val url =
          srcrefUrl(
            account = "user",
            repo = "repo",
            path = "file.kt",
            beginRegex = "pattern",
            endRegex = "",
          )
        url shouldNotContain "eregex"
        url shouldNotContain "eoccur"
        url shouldNotContain "eoffset"
        url shouldNotContain "etopd"
      }

      "srcrefUrl applies HTML4 escaping" {
        val url =
          srcrefUrl(
            account = "user",
            repo = "repo",
            path = "file.kt",
            beginRegex = "pattern",
            escapeHtml4 = true,
          )
        url shouldContain "&amp;"
      }

      "srcrefUrl uses custom prefix and branch" {
        val url =
          srcrefUrl(
            account = "user",
            repo = "repo",
            path = "file.kt",
            beginRegex = "pattern",
            prefix = "https://custom.com",
            branch = "develop",
          )
        url shouldStartWith "https://custom.com/github?"
        url shouldContain "branch=develop"
      }

      "srcrefUrl URL-encodes regex special characters" {
        val url =
          srcrefUrl(
            account = "user",
            repo = "repo",
            path = "file.kt",
            beginRegex = "foo\\(bar\\)",
          )
        url shouldContain "bregex="
        // The backslashes and parens should be URL-encoded, not literal
        url shouldNotContain "bregex=foo\\(bar\\)"
      }
    },
  )
