import com.pambrose.srcref.Urls.calcLineNumber
import io.kotest.core.spec.style.*
import io.kotest.matchers.*

class FileTests : StringSpec(
  {
    "String.toIntList()" {

      val input = """
        aaa
        aaa
        aaa
        bbb
        bbb
        bbb
        ccc
        ccc
        ccc
      """.lines().filter { it.isNotBlank() }

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
  })