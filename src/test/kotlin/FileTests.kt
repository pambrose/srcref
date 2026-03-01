import com.pambrose.srcref.Urls.calcLineNumber
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class FileTests :
  StringSpec(
    {
      "calcLineNumber top-down and bottom-up" {

        val input =
          """
        aaa
        aaa
        aaa
        bbb
        bbb
        bbb
        ccc
        ccc
        ccc
      """.lines()
            .filter {
              it.isNotBlank()
            }

        calcLineNumber(input, "aaa", 1, 0, true) shouldBe 1
        calcLineNumber(input, "aaa", 1, 1, true) shouldBe 2
        calcLineNumber(input, "aaa", 1, 2, true) shouldBe 3

        calcLineNumber(input, "aaa", 2, 0, true) shouldBe 2
        calcLineNumber(input, "aaa", 2, 1, true) shouldBe 3
        calcLineNumber(input, "aaa", 2, 2, true) shouldBe 4

        calcLineNumber(input, "bbb", 1, 0, true) shouldBe 4
        calcLineNumber(input, "bbb", 1, 1, true) shouldBe 5
        calcLineNumber(input, "bbb", 1, 2, true) shouldBe 6

        calcLineNumber(input, "bbb", 2, 0, true) shouldBe 5
        calcLineNumber(input, "bbb", 2, 1, true) shouldBe 6
        calcLineNumber(input, "bbb", 2, 2, true) shouldBe 7

        calcLineNumber(input, "aaa", 1, 0, false) shouldBe 3
        calcLineNumber(input, "aaa", 1, 1, false) shouldBe 4
        calcLineNumber(input, "aaa", 1, 2, false) shouldBe 5

        calcLineNumber(input, "aaa", 2, 0, false) shouldBe 2
        calcLineNumber(input, "aaa", 2, 1, false) shouldBe 3
        calcLineNumber(input, "aaa", 2, 2, false) shouldBe 4

        calcLineNumber(input, "bbb", 1, 0, false) shouldBe 6
        calcLineNumber(input, "bbb", 1, 1, false) shouldBe 7
        calcLineNumber(input, "bbb", 1, 2, false) shouldBe 8

        calcLineNumber(input, "bbb", 2, 0, false) shouldBe 5
        calcLineNumber(input, "bbb", 2, 1, false) shouldBe 6
        calcLineNumber(input, "bbb", 2, 2, false) shouldBe 7
      }

      "calcLineNumber with invalid regex throws" {
        shouldThrow<IllegalArgumentException> {
          calcLineNumber(listOf("abc"), "[invalid", 1, 0, true)
        }.message shouldContain "Invalid regex"
      }

      "calcLineNumber with no match found throws" {
        val input = listOf("aaa", "bbb", "ccc")
        shouldThrow<IllegalArgumentException> {
          calcLineNumber(input, "zzz", 1, 0, true)
        }.message shouldContain "not found"
      }

      "calcLineNumber with occurrence exceeding matches throws" {
        val input = listOf("aaa", "bbb", "ccc")
        shouldThrow<IllegalArgumentException> {
          calcLineNumber(input, "aaa", 5, 0, true)
        }.message shouldContain "not found"
      }

      "calcLineNumber with negative offset" {
        val input = listOf("aaa", "bbb", "ccc")
        calcLineNumber(input, "ccc", 1, -1, true) shouldBe 2
        calcLineNumber(input, "ccc", 1, -2, true) shouldBe 1
      }

      "calcLineNumber with regex special characters" {
        val input = listOf("foo(bar)", "baz.qux", "abc")
        calcLineNumber(input, "foo\\(bar\\)", 1, 0, true) shouldBe 1
        calcLineNumber(input, "baz\\.qux", 1, 0, true) shouldBe 2
      }

      "calcLineNumber with single-line input" {
        val input = listOf("only line")
        calcLineNumber(input, "only", 1, 0, true) shouldBe 1
      }

      "calcLineNumber with empty lines in input" {
        val input = listOf("", "content", "", "more")
        calcLineNumber(input, "content", 1, 0, true) shouldBe 2
        calcLineNumber(input, "more", 1, 0, true) shouldBe 4
      }

      "calcLineNumber desc appears in error messages" {
        val input = listOf("aaa")
        shouldThrow<IllegalArgumentException> {
          calcLineNumber(input, "zzz", 1, 0, true, "begin")
        }.message shouldContain "begin"
      }

      "calcLineNumber bottom-up with negative offset" {
        val input = listOf("aaa", "bbb", "ccc")
        // bottom-up: first match of "ccc" is at original index 2, line = 2 + (-1) + 1 = 2
        calcLineNumber(input, "ccc", 1, -1, false) shouldBe 2
      }

      "calcLineNumber with occurrence 0 throws" {
        val input = listOf("aaa", "bbb", "ccc")
        shouldThrow<IllegalArgumentException> {
          calcLineNumber(input, "aaa", 0, 0, true)
        }.message shouldContain "Occurrence must be >= 1"
      }

      "calcLineNumber with negative occurrence throws" {
        val input = listOf("aaa", "bbb", "ccc")
        shouldThrow<IllegalArgumentException> {
          calcLineNumber(input, "aaa", -1, 0, true)
        }.message shouldContain "Occurrence must be >= 1"
      }
    },
  )
