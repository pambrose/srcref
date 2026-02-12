import com.pambrose.srcref.QueryParams
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class QueryParamsTest :
  StringSpec(
    {
      "QueryParams has 12 entries" {
        QueryParams.entries.size shouldBe 12
      }

      "QueryParams entries have correct arg values" {
        QueryParams.ACCOUNT.arg shouldBe "account"
        QueryParams.REPO.arg shouldBe "repo"
        QueryParams.BRANCH.arg shouldBe "branch"
        QueryParams.PATH.arg shouldBe "path"
        QueryParams.BEGIN_REGEX.arg shouldBe "bregex"
        QueryParams.BEGIN_OCCURRENCE.arg shouldBe "boccur"
        QueryParams.BEGIN_OFFSET.arg shouldBe "boffset"
        QueryParams.BEGIN_TOPDOWN.arg shouldBe "btopd"
        QueryParams.END_REGEX.arg shouldBe "eregex"
        QueryParams.END_OCCURRENCE.arg shouldBe "eoccur"
        QueryParams.END_OFFSET.arg shouldBe "eoffset"
        QueryParams.END_TOPDOWN.arg shouldBe "etopd"
      }

      "optionalParams contains exactly the end params" {
        QueryParams.optionalParams shouldBe listOf("eregex", "eoccur", "eoffset", "etopd")
      }

      "defaultIfNull returns value when present" {
        val params = mapOf("account" to "testuser")
        QueryParams.ACCOUNT.defaultIfNull(params) shouldBe "testuser"
      }

      "defaultIfNull returns default when value is null" {
        val params = mapOf("boccur" to null)
        QueryParams.BEGIN_OCCURRENCE.defaultIfNull(params) shouldBe "1"
      }

      "defaultIfNull returns default when key is missing" {
        val params = emptyMap<String, String?>()
        QueryParams.BEGIN_OCCURRENCE.defaultIfNull(params) shouldBe "1"
      }

      "defaultIfNull returns blank when value is blank" {
        val params = mapOf("boccur" to "")
        QueryParams.BEGIN_OCCURRENCE.defaultIfNull(params) shouldBe ""
      }

      "defaultIfBlank returns value when non-blank" {
        val params = mapOf("boccur" to "3")
        QueryParams.BEGIN_OCCURRENCE.defaultIfBlank(params) shouldBe "3"
      }

      "defaultIfBlank returns default when blank" {
        val params = mapOf("boccur" to "")
        QueryParams.BEGIN_OCCURRENCE.defaultIfBlank(params) shouldBe "1"
      }

      "defaultIfBlank returns default when null" {
        val params = mapOf("boccur" to null)
        QueryParams.BEGIN_OCCURRENCE.defaultIfBlank(params) shouldBe "1"
      }

      "defaultIfBlank returns default when key missing" {
        val params = emptyMap<String, String?>()
        QueryParams.BEGIN_OCCURRENCE.defaultIfBlank(params) shouldBe "1"
      }

      "required returns value when non-blank" {
        val params = mapOf("account" to "testuser")
        QueryParams.ACCOUNT.required(params) shouldBe "testuser"
      }

      "required throws when blank" {
        val params = mapOf("account" to "")
        shouldThrow<IllegalArgumentException> {
          QueryParams.ACCOUNT.required(params)
        }.message shouldBe "Missing: account value"
      }

      "required throws when null" {
        val params = mapOf("account" to null)
        shouldThrow<IllegalArgumentException> {
          QueryParams.ACCOUNT.required(params)
        }.message shouldBe "Missing: account value"
      }

      "required throws when key missing" {
        val params = emptyMap<String, String?>()
        shouldThrow<IllegalArgumentException> {
          QueryParams.ACCOUNT.required(params)
        }.message shouldBe "Missing: account value"
      }

      "end params have correct defaults" {
        val params = emptyMap<String, String?>()
        QueryParams.END_REGEX.defaultIfNull(params) shouldBe ""
        QueryParams.END_OCCURRENCE.defaultIfNull(params) shouldBe "1"
        QueryParams.END_OFFSET.defaultIfNull(params) shouldBe "0"
        QueryParams.END_TOPDOWN.defaultIfNull(params) shouldBe "true"
      }
    },
  )
