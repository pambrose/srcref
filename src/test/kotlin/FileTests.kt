import Utils.calcLineNumber
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

      calcLineNumber(input, true, "aaa", 1, 0) shouldBe 1
      calcLineNumber(input, true, "aaa", 1, 1) shouldBe 2
      calcLineNumber(input, true, "aaa", 1, 2) shouldBe 3

      calcLineNumber(input, true, "aaa", 2, 0) shouldBe 2
      calcLineNumber(input, true, "aaa", 2, 1) shouldBe 3
      calcLineNumber(input, true, "aaa", 2, 2) shouldBe 4

      calcLineNumber(input, true, "bbb", 1, 0) shouldBe 4
      calcLineNumber(input, true, "bbb", 1, 1) shouldBe 5
      calcLineNumber(input, true, "bbb", 1, 2) shouldBe 6

      calcLineNumber(input, true, "bbb", 2, 0) shouldBe 5
      calcLineNumber(input, true, "bbb", 2, 1) shouldBe 6
      calcLineNumber(input, true, "bbb", 2, 2) shouldBe 7

      calcLineNumber(input, false, "aaa", 1, 0) shouldBe 3
      calcLineNumber(input, false, "aaa", 1, 1) shouldBe 4
      calcLineNumber(input, false, "aaa", 1, 2) shouldBe 5

      calcLineNumber(input, false, "aaa", 2, 0) shouldBe 2
      calcLineNumber(input, false, "aaa", 2, 1) shouldBe 3
      calcLineNumber(input, false, "aaa", 2, 2) shouldBe 4

      calcLineNumber(input, false, "bbb", 1, 0) shouldBe 6
      calcLineNumber(input, false, "bbb", 1, 1) shouldBe 7
      calcLineNumber(input, false, "bbb", 1, 2) shouldBe 8

      calcLineNumber(input, false, "bbb", 2, 0) shouldBe 5
      calcLineNumber(input, false, "bbb", 2, 1) shouldBe 6
      calcLineNumber(input, false, "bbb", 2, 2) shouldBe 7
    }
  })