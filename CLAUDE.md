# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**srcref** is a Kotlin web service that generates dynamic GitHub permalinks using regex patterns. Instead of linking to
fixed line numbers that break when code changes, srcref uses regex to find the target lines dynamically. Live
at https://www.srcref.com.

## Build Commands

```bash
./gradlew build -xtest        # Build without tests
./gradlew test                 # Run all tests (Kotest/JUnit5)
./gradlew check                # Full check including lint
./gradlew lintKotlin           # Lint only
./gradlew formatKotlin         # Auto-format code
./gradlew run                  # Run dev server (port 8080)
./gradlew buildFatJar          # Create fat JAR at build/libs/srcref-all.jar
./gradlew dependencyUpdates    # Check for dependency updates
```

Run a single test class:

```bash
./gradlew test --tests "FileTests"
```

Run a single named test (Kotest string spec name):

```bash
./gradlew test --tests "FileTests.calcLineNumber top-down and bottom-up"
```

Makefile shortcuts: `make build`, `make tests`, `make run`, `make uber`, `make release`.

## Architecture

**Entry point**: `Main.kt` — `object Main` with `@JvmStatic fun main()`. Starts a Ktor CIO embedded server, installs
middleware (CallLogging, DefaultHeaders, StatusPages, Compression), then calls `configureRoutes()`.

**Request flow** (e.g., `GET /github?account=X&repo=Y&...`):

1. `Routes.kt`: `readQueryParams()` builds a `Map<String, String?>` from all 12 `QueryParams` enum entries
2. `Urls.kt`: `githubRangeUrl()` validates params, constructs the raw GitHub URL
3. `ContentCache.kt`: `fetchContent()` fetches file content with ETag-based caching (304 support, LRU eviction)
4. `Urls.kt`: `calcLineNumber()` searches file lines by regex — supports occurrence selection (Nth match),
   top-down/bottom-up direction, and line offset. Has a 5-second regex timeout per search.
5. Builds `https://github.com/{account}/{repo}/blob/{branch}/{path}#L{begin}[-L{end}]` and 302-redirects the browser
6. On any error: redirects to `/problem?msg=<encoded>&<params>` instead

Core modules in `src/main/kotlin/com/pambrose/srcref/`:

- **Urls.kt** — Core logic. `calcLineNumber()` is the key algorithm; `githubRangeUrl()` orchestrates the full flow.
- **ContentCache.kt** — ETag-based HTTP cache using `ConcurrentHashMap`. Background daemon thread evicts LRU entries
  every 5 min when size exceeds `MAX_CACHE_SIZE`. HTTP client has retry logic (3 retries, exponential backoff).
- **QueryParams.kt** — Enum defining all 12 URL query parameters with defaults and validation. Begin params (`bregex`,
  `boccur`, `boffset`, `btopd`) are required; end params (`eregex`, `eoccur`, `eoffset`, `etopd`) are optional.
- **Routes.kt** — HTTP route definitions. Maps endpoints to handlers.
- **Endpoints.kt** — Enum mapping endpoint names to lowercase paths.
- **Api.kt** — Public `Api.srcrefUrl()` function for programmatic use (available as JitPack dependency).
- **pages/** — HTML pages using kotlinx.html DSL. `PageTemplate.kt` is the base template; `Edit.kt` (main form),
  `Error.kt`, `Version.kt`, `What.kt`, `Cache.kt` are individual pages. `Common.kt` has shared constants/utilities.

## Key Endpoints

| Path           | Purpose                               |
|----------------|---------------------------------------|
| `/edit`        | Main form UI                          |
| `/github`      | Redirect to computed GitHub permalink |
| `/github?edit` | Edit an existing srcref URL           |
| `/problem`     | Error display with message            |
| `/ping`        | Health check (`pong`)                 |
| `/cache`       | Cache status table                    |
| `/version`     | Version and build info                |
| `/threaddump`  | JVM thread dump (Dropwizard)          |

## Testing

Tests in `src/test/kotlin/` use Kotest `StringSpec` with JUnit5 runner:

- **FileTests.kt** — `calcLineNumber()`: top-down/bottom-up, offsets, invalid regex, occurrence out of range
- **UrlsTest.kt** — `toQueryParams()`, `srcrefToGithubUrl()`, `toInt()`, `githubRangeUrl()` with edge cases
- **ContentCacheTest.kt** — Cache CRUD, `markReferenced()`, `sortedByLastReferenced()`, volatile visibility
- **ApiTest.kt** — `Api.srcrefUrl()`: default/custom params, HTML escaping, URL encoding
- **QueryParamsTest.kt** — Enum entries, `defaultIfNull()`, `defaultIfBlank()`, `required()` validation
- **RoutesIntegrationTest.kt** — Full HTTP integration tests using `ktor-server-test-host`
- **BugFixTests.kt** — Regression tests: ETag edge cases, daemon thread, robots.txt, occurrence validation
- **EndpointsTest.kt**, **CommonTest.kt**, **MainTest.kt** — Unit tests for utilities

## Code Style

- Kotlinter (ktlint) for linting. Run `./gradlew formatKotlin` before committing.
- Disabled ktlint rules (via `.editorconfig`): `no-wildcard-imports`, `string-template-indent`, `indent`,
  `chain-method-continuation`, `import-ordering`.
- 120 char line length, 2-space indentation, UTF-8, LF line endings.

## Version Management

Version is defined in `build.gradle.kts` (`version = "2.0.1"`). Must also be updated in `Makefile` (`VERSION`) and
`README.md` when changing.

## Dependencies

Managed via `gradle/libs.versions.toml`. Key frameworks: Ktor (HTTP server/client), kotlinx.html (HTML DSL), Kotest (
testing), Dropwizard Metrics (JVM metrics), kotlin-logging + Logback. JVM toolchain: Java 17.

Build metadata (VERSION, RELEASE_DATE, BUILD_TIME) is generated at compile time by the `buildconfig` Gradle plugin into
a `BuildConfig` class.

## Environment Variables

| Variable         | Default                | Purpose                        |
|------------------|------------------------|--------------------------------|
| `PORT`           | 8080                   | HTTP server port               |
| `PREFIX`         | https://www.srcref.com | URL prefix for generated links |
| `DEFAULT_BRANCH` | master                 | Default GitHub branch          |
| `MAX_CACHE_SIZE` | 2048                   | Max cached file entries        |
| `MAX_LENGTH`     | 5MB                    | Max file size to process       |

## Deployment

Docker multi-arch build (amd64/arm64) via `make release`. Image runs on Alpine + OpenJDK 17 JRE.
Heroku supported via `system.properties` (Java 17 runtime). No CI/CD pipeline — deployment is manual.