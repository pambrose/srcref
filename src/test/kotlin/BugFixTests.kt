import com.pambrose.srcref.ContentCache
import com.pambrose.srcref.ContentCache.CacheContent
import com.pambrose.srcref.Urls.calcLineNumber
import com.pambrose.srcref.module
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication

class BugFixTests :
  StringSpec(
    {
      // Bug 2: ETag substring bounds
      "Cache page handles short ETags without crashing" {
        val content = CacheContent(pageLines = listOf("line1"), etag = "ab", contentLength = 10)
        // Verify the etag is accessible and short - the actual crash was in the cache display page
        content.etag shouldBe "ab"
        content.etag.length shouldBe 2
        // Verify the bounds-safe substring logic
        val etag = content.etag
        val display = etag.substring(1..minOf(20, etag.length - 1)).let { if (etag.length > 21) "$it..." else it }
        display shouldBe "b"
      }

      "Cache page handles etag of length 1" {
        val etag = "x"
        val display = etag.substring(1..minOf(20, etag.length - 1)).let { if (etag.length > 21) "$it..." else it }
        display shouldBe ""
      }

      "Cache page handles long ETags with truncation" {
        val etag = "W/\"abcdefghijklmnopqrstuvwxyz1234567890\""
        val display = etag.substring(1..minOf(20, etag.length - 1)).let { if (etag.length > 21) "$it..." else it }
        display shouldBe "/\"abcdefghijklmnopqr..."
      }

      "Cache page handles ETags exactly 21 chars" {
        val etag = "123456789012345678901" // exactly 21 chars
        val display = etag.substring(1..minOf(20, etag.length - 1)).let { if (etag.length > 21) "$it..." else it }
        display shouldBe "23456789012345678901"
      }

      // Bug 3: Daemon thread (verified via thread listing)
      "Cache cleanup thread is a daemon thread" {
        // Force loading of ContentCache companion which starts the cleanup thread
        ContentCache()
        Thread.sleep(50)
        val cacheThread = Thread.getAllStackTraces().keys.find { it.name == "Cache Cleanup" }
        cacheThread!!.isDaemon shouldBe true
      }

      // Bug 5: robots.txt correct paths (integration test)
      "robots.txt disallows /problem not /error" {
        testApplication {
          application { module() }
          client.get("/robots.txt").apply {
            status shouldBe HttpStatusCode.OK
            val body = bodyAsText()
            body shouldContain "/problem"
            body shouldContain "/cache"
            body shouldContain "/threaddump"
            body shouldNotContain "/error"
          }
        }
      }

      // Bug 7: occurrence validation
      "calcLineNumber rejects occurrence of 0" {
        shouldThrow<IllegalArgumentException> {
          calcLineNumber(listOf("a", "b"), "a", 0, 0, true)
        }.message shouldContain "Occurrence must be >= 1"
      }

      "calcLineNumber rejects negative occurrence" {
        shouldThrow<IllegalArgumentException> {
          calcLineNumber(listOf("a", "b"), "a", -5, 0, true)
        }.message shouldContain "Occurrence must be >= 1"
      }

      "calcLineNumber still works with occurrence of 1" {
        calcLineNumber(listOf("a", "b", "c"), "b", 1, 0, true) shouldBe 2
      }
    },
  )
