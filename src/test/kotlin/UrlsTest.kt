import com.pambrose.srcref.QueryParams
import com.pambrose.srcref.Urls.githubRangeUrl
import com.pambrose.srcref.Urls.srcrefToGithubUrl
import com.pambrose.srcref.Urls.toInt
import com.pambrose.srcref.Urls.toQueryParams
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith

class UrlsTest :
  StringSpec(
    {
      "toQueryParams with simple pairs" {
        mapOf("key" to "value").toQueryParams(false) shouldBe "key=value"
      }

      "toQueryParams filters null values" {
        mapOf("key1" to "value1", "key2" to null).toQueryParams(false) shouldBe "key1=value1"
      }

      "toQueryParams with ignoreEndParams true filters optional params" {
        val params = mapOf("account" to "user", "eregex" to "pattern", "eoccur" to "1")
        val result = params.toQueryParams(true)
        result shouldContain "account=user"
        result shouldNotContain "eregex"
        result shouldNotContain "eoccur"
      }

      "toQueryParams with ignoreEndParams false includes optional params" {
        val params = mapOf("account" to "user", "eregex" to "pattern")
        val result = params.toQueryParams(false)
        result shouldContain "account=user"
        result shouldContain "eregex=pattern"
      }

      "toQueryParams with empty map returns empty string" {
        emptyMap<String, String?>().toQueryParams(false) shouldBe ""
      }

      "toQueryParams URL-encodes special characters" {
        val result = mapOf("key" to "hello world").toQueryParams(false)
        result shouldContain "key="
        result shouldNotContain "hello world"
      }

      "srcrefToGithubUrl produces correct URL structure" {
        val params = mapOf("account" to "user", "repo" to "myrepo")
        val url = srcrefToGithubUrl(params, prefix = "https://www.srcref.com")
        url shouldStartWith "https://www.srcref.com/github?"
        url shouldContain "account=user"
        url shouldContain "repo=myrepo"
      }

      "srcrefToGithubUrl with escapeHtml4" {
        val params = mapOf("account" to "user", "repo" to "myrepo")
        val url = srcrefToGithubUrl(params, escapeHtml4 = true, prefix = "https://www.srcref.com")
        url shouldContain "&amp;"
      }

      "srcrefToGithubUrl ignores end params when eregex is blank" {
        val params = mapOf("account" to "user", "eregex" to "")
        val url = srcrefToGithubUrl(params, prefix = "https://www.srcref.com")
        url shouldNotContain "eregex"
      }

      "srcrefToGithubUrl includes end params when eregex is present" {
        val params = mapOf("account" to "user", "eregex" to "pattern")
        val url = srcrefToGithubUrl(params, prefix = "https://www.srcref.com")
        url shouldContain "eregex=pattern"
      }

      "toInt with valid int" {
        "42".toInt { "error" } shouldBe 42
      }

      "toInt with negative int" {
        "-5".toInt { "error" } shouldBe -5
      }

      "toInt with null throws" {
        shouldThrow<IllegalArgumentException> {
          (null as String?).toInt { "custom error" }
        }.message shouldBe "custom error"
      }

      "toInt with non-numeric throws" {
        shouldThrow<IllegalArgumentException> {
          "abc".toInt { "custom error" }
        }.message shouldBe "custom error"
      }

      "toInt with empty string throws" {
        shouldThrow<IllegalArgumentException> {
          "".toInt { "custom error" }
        }.message shouldBe "custom error"
      }

      "toInt with decimal throws" {
        shouldThrow<IllegalArgumentException> {
          "1.5".toInt { "custom error" }
        }.message shouldBe "custom error"
      }

      "githubRangeUrl with empty params returns empty pair" {
        val (url, msg) = githubRangeUrl(emptyMap(), "http://localhost:8080")
        url shouldBe ""
        msg shouldBe ""
      }

      "githubRangeUrl with all-blank params returns empty pair" {
        val params = QueryParams.entries.associate { it.arg to "" }
        val (url, msg) = githubRangeUrl(params, "http://localhost:8080")
        url shouldBe ""
        msg shouldBe ""
      }

      "githubRangeUrl with missing required param returns problem URL" {
        val params = mapOf("account" to "test", "repo" to null, "branch" to null, "path" to null)
        val (url, msg) = githubRangeUrl(params, "http://localhost:8080")
        url shouldContain "problem"
        msg shouldContain "Missing: repo value"
      }
    },
  )
