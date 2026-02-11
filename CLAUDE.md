# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**srcref** is a Kotlin web service that generates dynamic GitHub permalinks using regex patterns. Instead of linking to
fixed line numbers that break when code changes, srcref uses regex to find the target lines dynamically. Live
at https://www.srcref.com.

## Build Commands

```bash
./gradlew build -xtest        # Build without tests
./gradlew test                 # Run tests (Kotest/JUnit5)
./gradlew check                # Full check including lint
./gradlew lintKotlin           # Lint only
./gradlew formatKotlin         # Auto-format code
./gradlew run                  # Run dev server (port 8080)
./gradlew buildFatJar          # Create fat JAR at build/libs/srcref-all.jar
./gradlew dependencyUpdates    # Check for dependency updates
```

Makefile shortcuts: `make build`, `make tests`, `make run`, `make uberjar`, `make release`.

## Architecture

**Entry point**: `Main.kt` — Embeds Ktor CIO server, installs middleware (CallLogging, Compression, StatusPages).

**Request flow**: `Main.kt` → `Routes.kt` → handler logic

Core modules in `src/main/kotlin/com/pambrose/srcref/`:

- **Routes.kt** — HTTP route definitions. Maps endpoints to handlers, reads query params from requests.
- **Urls.kt** — Core logic. `calcLineNumber()` is the key algorithm: searches file lines by regex with occurrence
  selection, top-down/bottom-up direction, and line offset. `githubRangeUrl()` orchestrates fetching content and
  building the GitHub URL.
- **ContentCache.kt** — ETag-based HTTP cache for GitHub raw file content. LRU eviction via background thread (5-min
  intervals). Uses `ConcurrentHashMap`. Configurable via `MAX_CACHE_SIZE` (default 2048) and `MAX_LENGTH` (default 5MB)
  env vars.
- **QueryParams.kt** — Enum defining all URL query parameters with defaults and validation. Begin params (`bregex`,
  `boccur`, `boffset`, `btopd`) are required; end params (`eregex`, `eoccur`, `eoffset`, `etopd`) are optional.
- **Endpoints.kt** — Enum mapping endpoint names to lowercase paths.
- **Api.kt** — Public `srcrefUrl()` function for programmatic use.
- **pages/** — HTML pages using kotlinx.html DSL. `PageTemplate.kt` is the base template; `Edit.kt` (main form),
  `Error.kt`, `Version.kt`, `What.kt`, `Cache.kt` are individual pages.

## Key Endpoints

| Path           | Purpose                               |
|----------------|---------------------------------------|
| `/edit`        | Main form UI                          |
| `/github`      | Redirect to computed GitHub permalink |
| `/github?edit` | Edit an existing srcref URL           |
| `/problem`     | Error display with message            |
| `/ping`        | Health check                          |

## Testing

Tests are in `src/test/kotlin/FileTests.kt` using Kotest `StringSpec`. Tests exercise `calcLineNumber()` with various
combinations of pattern, occurrence, offset, and direction (top-down vs bottom-up).

```bash
./gradlew test    # Run all tests
```

## Code Style

- Kotlinter (ktlint) for linting. Run `./gradlew formatKotlin` before committing.
- Disabled ktlint rules (via `.editorconfig`): `no-wildcard-imports`, `string-template-indent`, `indent`,
  `chain-method-continuation`.
- 120 char line length, 2-space indentation, UTF-8, LF line endings.

## Dependencies

Managed via `gradle/libs.versions.toml`. Key frameworks: Ktor (HTTP server/client), kotlinx.html (HTML DSL), Kotest (
testing), Dropwizard Metrics (JVM metrics), kotlin-logging + Logback.

## Version Management

Version is defined in `build.gradle.kts` (`version = "1.9.8"`). Must also be updated in `Makefile` (`VERSION`) and
`README.md` when changing.

## Environment Variables

| Variable         | Default                | Purpose                        |
|------------------|------------------------|--------------------------------|
| `PORT`           | 8080                   | HTTP server port               |
| `PREFIX`         | https://www.srcref.com | URL prefix for generated links |
| `DEFAULT_BRANCH` | master                 | Default GitHub branch          |
| `MAX_CACHE_SIZE` | 2048                   | Max cached file entries        |
| `MAX_LENGTH`     | 5MB                    | Max file size to process       |
