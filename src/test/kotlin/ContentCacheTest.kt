import com.pambrose.srcref.ContentCache
import com.pambrose.srcref.ContentCache.CacheContent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ContentCacheTest :
  StringSpec(
    {
      "CacheContent stores pageLines, etag, and contentLength" {
        val content = CacheContent(pageLines = listOf("line1", "line2"), etag = "abc123", contentLength = 100)
        content.pageLines shouldBe listOf("line1", "line2")
        content.etag shouldBe "abc123"
        content.contentLength shouldBe 100
      }

      "CacheContent hits starts at 0" {
        val content = CacheContent(pageLines = listOf("line1"), etag = "abc", contentLength = 10)
        content.hits shouldBe 0
      }

      "CacheContent markReferenced increments hits" {
        val content = CacheContent(pageLines = listOf("line1"), etag = "abc", contentLength = 10)
        content.markReferenced()
        content.hits shouldBe 1
        content.markReferenced()
        content.hits shouldBe 2
      }

      "CacheContent age and lastReferenced are non-negative" {
        val content = CacheContent(pageLines = listOf("line1"), etag = "abc", contentLength = 10)
        content.age.isNegative() shouldBe false
        content.lastReferenced.isNegative() shouldBe false
      }

      "ContentCache set and get" {
        val cache = ContentCache()
        val content = CacheContent(pageLines = listOf("line1"), etag = "abc", contentLength = 10)
        cache["http://example.com"] = content
        cache["http://example.com"] shouldNotBe null
        cache["http://example.com"]!!.etag shouldBe "abc"
      }

      "ContentCache get returns null for missing key" {
        val cache = ContentCache()
        cache["http://nonexistent.com"] shouldBe null
      }

      "ContentCache size reflects entries" {
        val cache = ContentCache()
        cache.size shouldBe 0
        cache["url1"] = CacheContent(listOf(), "e1", 10)
        cache.size shouldBe 1
        cache["url2"] = CacheContent(listOf(), "e2", 20)
        cache.size shouldBe 2
      }

      "ContentCache remove works" {
        val cache = ContentCache()
        val content = CacheContent(listOf("line"), "etag", 10)
        cache["url"] = content
        cache.remove("url") shouldNotBe null
        cache["url"] shouldBe null
      }

      "ContentCache remove nonexistent returns null" {
        val cache = ContentCache()
        cache.remove("nonexistent") shouldBe null
      }

      "ContentCache sortedByLastReferenced ordering" {
        val cache = ContentCache()
        val content1 = CacheContent(listOf(), "e1", 10)
        cache["url1"] = content1
        Thread.sleep(15)
        val content2 = CacheContent(listOf(), "e2", 20)
        cache["url2"] = content2
        // url2 was created later, so its lastReferenced elapsed time is smaller
        // sortedBy ascending: smallest elapsed (most recent) first
        val sorted = cache.sortedByLastReferenced()
        sorted.first().key shouldBe "url2"
        sorted.last().key shouldBe "url1"
      }

      "ContentCache overwrite replaces entry" {
        val cache = ContentCache()
        cache["url"] = CacheContent(listOf("old"), "e1", 10)
        cache["url"] = CacheContent(listOf("new"), "e2", 20)
        cache["url"]!!.pageLines shouldBe listOf("new")
        cache["url"]!!.etag shouldBe "e2"
        cache.size shouldBe 1
      }
    },
  )
