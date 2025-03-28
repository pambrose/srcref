package com.pambrose.srcref

import com.github.pambrose.common.util.isNotNull
import com.github.pambrose.common.util.simpleClassName
import com.pambrose.srcref.Urls.RAW_PREFIX
import com.pambrose.srcref.Urls.toInt
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.sortedBy
import kotlin.collections.sortedByDescending
import kotlin.collections.take
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.plusAssign
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource.Monotonic

internal class ContentCache {
  private val contentMap: ConcurrentHashMap<String, CacheContent> = ConcurrentHashMap()
  private val maxCacheSize = System.getenv("MAX_CACHE_SIZE")?.toInt() ?: 2048
  private val maxLength = System.getenv("MAX_LENGTH")?.toInt() ?: (5 * 1048576)

  class CacheContent(
    internal val pageLines: List<String>,
    internal val etag: String,
    internal val contentLength: Int,
    private val references: AtomicInt = AtomicInt(0),
    private var referenced: TimeMark = Monotonic.markNow(),
    private val created: TimeMark = Monotonic.markNow(),
  ) {
    val age: Duration get() = created.elapsedNow()
    val lastReferenced: Duration get() = referenced.elapsedNow()
    val hits get() = references.load()

    fun markReferenced() {
      referenced = Monotonic.markNow()
      references += 1
    }
  }

  val size get() = contentMap.size

  fun sortedByLastReferenced(): List<Map.Entry<String, CacheContent>> =
    contentMap.entries.sortedBy { it.value.lastReferenced }

  fun remove(url: String) = contentMap.remove(url)

  operator fun set(
    url: String,
    value: CacheContent,
  ) {
    contentMap[url] = value
  }

  operator fun get(url: String): CacheContent? = contentMap[url]

  companion object {
    private val logger = KotlinLogging.logger {}
    internal val contentCache = ContentCache()

    init {
      logger.info { "Starting cache cleanup thread" }

      thread(name = "Cache Cleanup") {
        while (true) {
          runCatching {
            val overflow = contentCache.size - contentCache.maxCacheSize
            if (overflow > 0) {
              logger.info { "Cache size: ${contentCache.size} exceeds max: ${contentCache.maxCacheSize}" }
              contentCache.contentMap.entries.sortedByDescending { it.value.lastReferenced }.take(overflow)
                .forEach { (k, _) ->
                  logger.info { "Removing $k from cache" }
                  contentCache.remove(k)
                }
            }
          }.onFailure { e ->
            logger.error(e) { "Exception in Cache Cleanup ${e.simpleClassName} ${e.message}" }
          }
          Thread.sleep(5.minutes.inWholeMilliseconds)
        }
      }
    }

    private fun String.isInvalidContentType() =
      startsWith("application/") ||
        startsWith("image/") ||
        startsWith("video/") ||
        startsWith("model/") ||
        startsWith("font/") ||
        startsWith("audio/")

    internal suspend fun fetchContent(url: String): List<String> {
      val cacheItem = contentCache[url]
      val response =
        HttpClient(CIO) {
          install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
          }
        }.let { client ->
          client.get(url) {
            if (cacheItem.isNotNull() && cacheItem.etag.isNotBlank()) {
              logger.info { "Setting ETag: ${cacheItem.etag} for url: ${url.removePrefix(RAW_PREFIX)}" }
              headers.append(HttpHeaders.IfNoneMatch, cacheItem.etag)
            }
          }
        }

      response.headers[HttpHeaders.ContentType]?.also { contentType ->
        if (contentType.isInvalidContentType()) {
          val msg = "Invalid content type: $contentType"
          logger.warn { msg }
          throw IllegalArgumentException(msg)
        } else if (!contentType.startsWith("text/")) {
          logger.warn { "Unanticipated content type: $contentType" }
        }
      }

      return when {
        response.status == HttpStatusCode.NotModified && cacheItem.isNotNull() -> {
          cacheItem.run {
            logger.info { "Returning cached content for ETag: $etag and url: ${url.removePrefix(RAW_PREFIX)}" }
            markReferenced()
            pageLines
          }
        }

        else -> {
          val contentLength = HttpHeaders.ContentLength
          val length = response.headers[contentLength].toInt { "$contentLength header is null or not an integer" }
          if (length >= contentCache.maxLength) {
            val msg = "$contentLength exceeds maximum length: $length"
            logger.warn { msg }
            throw IllegalArgumentException(msg)
          }

          val etag = response.headers[HttpHeaders.ETag] ?: ""
          val pageLines = response.body<String>().lines()
          if (etag.isNotBlank()) {
            logger.info { "Adding item to content cache -- ETag: $etag and url: ${url.removePrefix(RAW_PREFIX)}" }
            val now = Monotonic.markNow()
            contentCache[url] =
              CacheContent(
                pageLines = pageLines,
                etag = etag,
                contentLength = length,
                referenced = now,
                created = now,
              ).apply { markReferenced() }
          }
          pageLines
        }
      }
    }
  }
}
