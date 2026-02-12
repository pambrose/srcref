import com.pambrose.srcref.pages.Common.WIDTH_VAL
import com.pambrose.srcref.pages.Common.hasValues
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CommonTest :
  StringSpec(
    {
      "hasValues returns false for empty map" {
        emptyMap<String, String?>().hasValues() shouldBe false
      }

      "hasValues returns false for all-null values" {
        mapOf("a" to null, "b" to null).hasValues() shouldBe false
      }

      "hasValues returns false for all-blank values" {
        mapOf("a" to "", "b" to "  ").hasValues() shouldBe false
      }

      "hasValues returns true when at least one non-blank value" {
        mapOf("a" to "", "b" to "test").hasValues() shouldBe true
      }

      "WIDTH_VAL is 93" {
        WIDTH_VAL shouldBe "93"
      }
    },
  )
