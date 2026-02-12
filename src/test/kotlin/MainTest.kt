import com.pambrose.srcref.startsWithList
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MainTest :
  StringSpec(
    {
      "startsWithList returns true when prefix matches" {
        "/ping".startsWithList(listOf("/ping", "/pong")) shouldBe true
      }

      "startsWithList returns false when no match" {
        "/other".startsWithList(listOf("/ping", "/pong")) shouldBe false
      }

      "startsWithList returns false for empty list" {
        "/anything".startsWithList(emptyList()) shouldBe false
      }

      "startsWithList with empty string input" {
        "".startsWithList(listOf("/ping")) shouldBe false
      }

      "startsWithList with partial prefix match" {
        "/pin".startsWithList(listOf("/ping")) shouldBe false
      }
    },
  )
