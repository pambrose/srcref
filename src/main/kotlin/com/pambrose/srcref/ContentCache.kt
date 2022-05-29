package com.pambrose.srcref

import com.github.pambrose.common.util.*
import com.pambrose.srcref.Urls.toInt
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import mu.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import kotlin.time.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource.Monotonic

internal class ContentCache {
  private val contentMap: ConcurrentHashMap<String, CacheContent> = ConcurrentHashMap()
  private val maxCacheSize = System.getenv("MAX_CACHE_SIZE")?.toInt() ?: 2048
  private val maxLength = System.getenv("MAX_LENGTH")?.toInt() ?: (5 * 1048576)

  class CacheContent(
    internal val content: List<String>,
    internal val etag: String,
    internal val contentLength: Int,
    private val references: AtomicInteger = AtomicInteger(0),
    private var referenced: TimeMark = Monotonic.markNow(),
    private val created: TimeMark = Monotonic.markNow(),
  ) {
    val age: Duration get() = created.elapsedNow()
    val lastReferenced: Duration get() = referenced.elapsedNow()
    val hits get() = references.get()

    fun markReferenced() {
      referenced = Monotonic.markNow()
      references.incrementAndGet()
    }
  }

  val size get() = contentMap.size

  fun sortedByLastReferenced(): List<Map.Entry<String, CacheContent>> =
    contentMap.entries.sortedBy { it.value.lastReferenced }

  fun remove(url: String) = contentMap.remove(url)

  operator fun set(url: String, value: CacheContent) {
    contentMap[url] = value
  }

  operator fun get(url: String): CacheContent? = contentMap[url]

  companion object : KLogging() {
    internal val contentCache = ContentCache()

    init {
      logger.info { "Starting cache cleanup thread" }

      newSingleThreadContext("Cache Cleanup").executor.execute {
        while (true) {
          try {
            val overflow = contentCache.size - contentCache.maxCacheSize
            if (overflow > 0) {
              logger.info { "Cache size: ${contentCache.size} exceeds max: ${contentCache.maxCacheSize}" }
              contentCache.contentMap.entries.sortedByDescending { it.value.lastReferenced }.take(overflow)
                .forEach { (k, _) ->
                  logger.info { "Removing $k from cache" }
                  contentCache.remove(k)
                }
            }
          } catch (e: Throwable) {
            logger.error(e) { "Exception in Cache Cleanup ${e.simpleClassName} ${e.message}" }
          }
          Thread.sleep(5.minutes.inWholeMilliseconds)
        }
      }
    }

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
              logger.info { "Setting ETag: ${cacheItem.etag} for url: $url" }
              headers.append(HttpHeaders.IfNoneMatch, cacheItem.etag)
            }
          }
        }

      return if (response.status == HttpStatusCode.NotModified && cacheItem.isNotNull()) {
        cacheItem.run {
          logger.info { "Returning cached content for ETag: $etag and url: $url" }
          markReferenced()
          content
        }
      } else {
        val cl = HttpHeaders.ContentLength
        val length = response.headers[cl].toInt { "$cl header is null or not an integer" }
        if (length >= contentCache.maxLength) {
          val msg = "$cl exceeds maximum length: $length"
          logger.warn { msg }
          throw IllegalArgumentException(msg)
        }

        val etag = response.headers[HttpHeaders.ETag] ?: ""
        val pageLines = response.body<String>().lines()
        if (etag.isNotBlank()) {
          logger.info { "Adding item to content cache -- ETag: $etag and url: $url" }
          contentCache[url] = CacheContent(pageLines, etag, length)
        }
        pageLines
      }
    }
  }
}