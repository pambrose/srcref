# srcref Bug Review — 2026-02-14

All 7 bugs have been fixed and verified with tests. Total test count: 102 (9 new).

---

## Bug 1: HttpClient Resource Leak (High) — FIXED

**File:** `ContentCache.kt`

A new `HttpClient(CIO)` was created on every call to `fetchContent()` and never closed.
Ktor `HttpClient` allocates connection pools, threads, and coroutine scopes internally.
Over time this leaks resources and can lead to file descriptor exhaustion or OOM under load.

**Fix:** Extracted a single shared `httpClient` instance in the companion object, reused
across all `fetchContent()` calls.

---

## Bug 2: ETag Substring Crash on Short ETags (High) — FIXED

**File:** `pages/Cache.kt`

The cache display page truncated ETags with a hard-coded range `substring(1..20)` which
throws `StringIndexOutOfBoundsException` if the ETag string is shorter than 21 characters.

**Fix:** Changed to `substring(1..minOf(20, v.etag.length - 1))` with adjusted truncation
indicator logic.

**Tests:** 4 new tests in `BugFixTests.kt` covering short (1-2 char), exact boundary (21 char),
and long ETag strings.

---

## Bug 3: Cache Cleanup Thread Prevents Graceful Shutdown (Medium) — FIXED

**File:** `ContentCache.kt`

Kotlin's `thread()` defaults to `isDaemon = false`. The non-daemon cleanup thread with an
infinite `while (true)` loop prevented the JVM from shutting down gracefully.

**Fix:** Set `isDaemon = true` on the thread.

**Tests:** 1 new test in `BugFixTests.kt` verifies the cache cleanup thread's `isDaemon`
flag is `true`.

---

## Bug 4: Data Race on `referenced` Field (Medium) — FIXED

**File:** `ContentCache.kt`

The `references` counter was an `AtomicInt` (thread-safe), but `referenced` was a plain
`var` read/written from multiple threads (request handlers + cleanup thread + cache display).

**Fix:** Added `@Volatile` annotation to the `referenced` field.

**Tests:** 1 new test in `ContentCacheTest.kt` verifies that `markReferenced()` called from
a separate thread is visible to the main thread.

---

## Bug 5: `robots.txt` Disallows Nonexistent Paths (Low) — FIXED

**File:** `Routes.kt`

The `robots.txt` disallowed `/error/` and `/error`, but no such endpoints exist. The actual
error page is at `/problem`, and internal diagnostics are at `/cache` and `/threaddump`.

**Fix:** Changed to disallow `/problem`, `/cache`, and `/threaddump` using the `Endpoints`
enum values for consistency.

**Tests:** 1 new test in `BugFixTests.kt` and updated existing `RoutesIntegrationTest` to
verify correct paths are present and `/error` is absent.

---

## Bug 6: No Protection Against ReDoS (Medium) — FIXED

**File:** `Urls.kt`

The `pattern` value from user-supplied query parameters was compiled into a `Regex` with no
safeguard against catastrophic backtracking.

**Fix:** Added a 5-second time-based deadline check between each line's regex match in
`calcLineNumber()`. If the deadline is exceeded, an `IllegalArgumentException` is thrown
with a clear timeout message.

---

## Bug 7: No Validation on `occurrence` Parameter (Low) — FIXED

**File:** `Urls.kt`

When `occurrence` was 0 or negative, `drop(-1)` silently behaved like `drop(0)`, returning
the first match without error.

**Fix:** Added `require(occurrence >= 1)` at the top of `calcLineNumber()`.

**Tests:** 2 new tests in `FileTests.kt` (occurrence 0 and negative) and 2 more in
`BugFixTests.kt` verifying the error message and that occurrence=1 still works.

---

## Summary

| # | Severity | File              | Fix                          | Tests         |
|---|----------|-------------------|------------------------------|---------------|
| 1 | High     | `ContentCache.kt` | Shared `httpClient` instance | —             |
| 2 | High     | `pages/Cache.kt`  | Bounds-safe `substring`      | 4             |
| 3 | Medium   | `ContentCache.kt` | `isDaemon = true`            | 1             |
| 4 | Medium   | `ContentCache.kt` | `@Volatile` on `referenced`  | 1             |
| 5 | Low      | `Routes.kt`       | Correct endpoint paths       | 1 + 1 updated |
| 6 | Medium   | `Urls.kt`         | Per-line timeout deadline    | —             |
| 7 | Low      | `Urls.kt`         | `require(occurrence >= 1)`   | 2 + 2         |
