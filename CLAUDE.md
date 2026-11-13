# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**srcref** is a Kotlin web service that generates dynamic GitHub permalinks using regex patterns. Instead of linking to
fixed line numbers that break when code changes, srcref uses regex to find the target lines dynamically. Live
at https://www.srcref.com.

## Build Commands

```bash
./gradlew build -x test        # Build without tests
./gradlew test                 # Run all tests (Kotest/JUnit5)
./gradlew check                # Full check including lint
./gradlew lintKotlin           # Lint only
./gradlew formatKotlin         # Auto-format code
./gradlew detekt               # Run detekt static analysis (also runs as part of `check`)
./gradlew detektBaseline       # Regenerate detekt-baseline.xml from current violations
./gradlew run                  # Run dev server (port 8080)
./gradlew buildFatJar          # Create fat JAR at build/libs/srcref-all.jar
./gradlew koverHtmlReport      # Run tests + generate HTML coverage report at build/reports/kover/html/
./gradlew koverXmlReport       # Run tests + generate XML coverage report (for CI tools)
./gradlew koverVerify          # Run tests + verify coverage rules
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

Makefile shortcuts: `make build`, `make tests`, `make ci` (run the CI workflows' build steps locally; see
Continuous Integration), `make run`, `make uber`, `make release` and `make release-check` (see Releasing),
`make log` (last `LOG_LINES`, default 200, of the Railway deployment log), `make railway-restart`,
`make railway-redeploy` (redeploys the latest image), `make railway-version` (the version the running app reports
at `/version` on the service's Railway domain, plus the active deployment and image digest; all four Railway targets
need a one-time `railway link`),
`make kdocs`, `make coverage`, `make coverage-xml`, `make coverage-verify`,
`make coverage-packages` (per-package coverage summary via `scripts/coverage_packages.py`), `make lint`
(kotlinter + detekt), `make detekt-baseline`, `make zizmor` (audit GitHub Actions workflows and `dependabot.yml`
via `uvx zizmor`), `make publish-local`, `make publish-maven-central`, `make check-site`,
`make upgrade-site` (check for / apply website dependency updates), `make upgrade-wrapper`
(upgrade the Gradle wrapper to the `gradle-wrapper` version in `libs.versions.toml`). Run `make help` to list
all annotated targets with descriptions; it is also the default `make` target.

`make versions` runs `./gradlew dependencyUpdates` and lists only the dependencies with newer versions (and any it
could not check), via the `outputFormatter` on `DependencyUpdatesTask` in `build.gradle.kts`. `make versions-all`
passes `--output-formatter=plain` for the plugin's full report, which also lists every up-to-date dependency.

## Architecture

**Entry point**: `Main.kt` — `object Main` with `@JvmStatic fun main()`. Starts a Ktor CIO embedded server, installs
middleware (CallLogging, DefaultHeaders, StatusPages, Compression, AutoHeadResponse), then calls `configureRoutes()`.

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
- **Api.kt** — Public `Api.srcrefUrl()` function for programmatic use (available as Maven Central dependency).
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
| `/what`        | About srcref                          |
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

Coverage is reported to Codecov via Kover. Codecov behavior is configured in `codecov.yml`:
project status uses `auto` target with a 0.5% threshold; patch status targets 70% but is
informational (won't gate PRs); generated `BuildConfig.kt` is ignored.

Test log output is quieted by `src/test/resources/logback-test.xml`, which sets the `io.ktor` logger to
`WARN` so Ktor test-host startup and `CallLogging` request logs don't clutter test runs.

## Code Style

- Kotlinter (ktlint) for linting/formatting. Run `./gradlew formatKotlin` before committing.
- Detekt for static analysis (code smells, complexity, potential bugs). Config in `config/detekt/detekt.yml`;
  pre-existing violations are snapshotted in `config/detekt/detekt-baseline.xml`. Both run as part of
  `./gradlew check`. After intentional new violations or fixes, regenerate the baseline with
  `./gradlew detektBaseline`.
- Disabled ktlint rules (via `.editorconfig`): `no-wildcard-imports`, `string-template-indent`, `indent`,
  `chain-method-continuation`, `import-ordering`.
- 120 char line length, 2-space indentation, UTF-8, LF line endings.
- `.gitattributes` normalizes line endings on commit (`* text=auto`), pins `gradlew` to LF (CRLF breaks it on
  Linux, including CI) and `*.bat` to CRLF, and marks `website/uv.lock` generated / `website/srcref/docs/**`
  as documentation for GitHub language stats.

## Version Management

Version is defined in `gradle.properties` (`version=2.4.1`). The Makefile `VERSION` is derived automatically from
`gradle.properties`. The following must still be updated manually when changing the version:

- `README.md` (Maven/Gradle dependency snippets and Kotlin version badge)
- `website/srcref/docs/api.md` (dependency snippets)
- `website/srcref/docs/getting-started.md` (dependency snippets)
- `CHANGELOG.md` and `RELEASE_NOTES.md` (new version section)

`make release-check` fails unless each of the first three files mentions the new version. That list is
`RELEASE_VERSION_FILES` in the `Makefile`; keep the two in step.

The build is reproducible: pass `-PreleaseDate=MM/dd/yyyy` and `-PbuildTime=<epoch-ms>` to override the values
embedded in `BuildConfig`. Without overrides they default to "now".

`llms.txt` exists in two identical copies with no build step or script to sync them: the repo-root copy, and
`src/main/resources/public/llms.txt`, which Ktor serves at https://www.srcref.com/llms.txt via `staticResources`.
Edit both together. Neither mentions the version, so a version bump alone does not touch them.

## Dependencies

Managed via `gradle/libs.versions.toml`. Key frameworks: Ktor (HTTP server/client), kotlinx.html (HTML DSL), Kotest (
testing), Dropwizard Metrics (JVM metrics), kotlin-logging + Logback, Dokka (KDoc generation). JVM toolchain: Java 17
(`jvm` in `libs.versions.toml`), so the code and the published library target 17. The tests run on Java 25 (`jre`), the
Java the Docker image runs; Gradle downloads that JDK through the foojay toolchain resolver if it isn't installed. The
test JVM, like the image's, gets `--enable-native-access=ALL-UNNAMED` so Jansi's native library loads without a warning.

Build metadata (VERSION, RELEASE_DATE, BUILD_TIME) is generated at compile time by the `buildconfig` Gradle plugin into
a `BuildConfig` class.

## Publishing

Published to Maven Central as `com.pambrose:srcref` via the vanniktech maven-publish plugin (`com.vanniktech.maven.publish`).
Dokka generates the javadoc jar from KDoc comments. Signing uses in-memory GPG keys via `signingInMemoryKey`,
`signingInMemoryKeyId`, and `signingInMemoryKeyPassword` Gradle properties.

The Maven Central targets (`publish-maven-central`, `publish-snapshot`) pass two sets of credentials to Gradle as
`ORG_GRADLE_PROJECT_*` environment variables, for that one command only:

- **Signing** (`GPG_ENV`): the secret key of `GPG_SIGNING_KEY_ID` (an environment variable), exported with
  `gpg --armor`, and its passphrase from the macOS keychain entry `gradle-signing-password` (account `gpg-signing`).
- **Maven Central** (`MAVEN_CENTRAL_ENV`): a user token's name and password from central.sonatype.com, from the
  keychain entries `maven-central-username` and `maven-central-password` (account `maven-central`). They become the
  vanniktech plugin's `mavenCentralUsername` and `mavenCentralPassword`. Keep them out of
  `~/.gradle/gradle.properties`, which every Gradle build on the machine can read. Every repository that publishes
  this way shares these entries. Add each one once; `-w` with no value makes `security` prompt for it, which keeps
  the secret out of shell history:

  ```bash
  security add-generic-password -a maven-central -s maven-central-username -U -w
  security add-generic-password -a maven-central -s maven-central-password -U -w
  ```

Before running Gradle, the `_check-gpg-env` and `_check-maven-central-env` preflight targets fail if any of these is
missing or empty. The error names the missing keychain entry but never prints its value.

- `make publish-local` — publish to `~/.m2/repository` for local testing. Note: this build only resolves from
  `mavenCentral()` (see `settings.gradle.kts`); to consume locally-published artifacts, add `mavenLocal()` to the
  consuming build's repositories. Needs no credentials.
- `make publish-maven-central` — publish and release to Maven Central (`make release` runs it as its last step).
- `make publish-snapshot` — publish `VERSION-SNAPSHOT` to Maven Central.
- `make kdocs` — generate KDoc HTML documentation to `build/dokka/html/`.

## Releasing

Bump the version and update the files listed under Version Management, merge to `master`, and let CI finish. Then
`make release`, run from a clean, up-to-date `master` checkout, first runs `make release-check`, which publishes
nothing and fails unless:

- the working tree is clean, on `master` (`RELEASE_BRANCH`), and matches `origin/master`;
- every GitHub check run for HEAD has passed;
- `CHANGELOG.md` has a `[VERSION]` section, `RELEASE_NOTES.md` a `[vVERSION]` one, and every file in
  `RELEASE_VERSION_FILES` mentions the version;
- the version has no pre-release (`-…`) suffix, its tag (if one exists) points at HEAD, and it isn't already on
  Docker Hub or Maven Central;
- the GPG signing setup is in place (`_check-gpg-env`), and Docker is running.

It then runs `make tests` and `make docker-prebuild` (every image platform, built but not pushed), and asks once,
refusing if HEAD moved or the working tree changed during those steps. After a `y` it runs `make docker-push`,
`make docker-verify` (both platforms present, `:latest` matches `:VERSION`), and `make publish-maven-central`. Docker
Hub goes first because its tags can be overwritten while a Maven Central release can't be removed, and because a
pushed tag shows on Docker Hub at once (repo1.maven.org can take a while to list a release), so the Docker Hub check
is what stops an accidental second run. It ends by printing the manual steps: the GitHub release, then
`make railway-redeploy`, which runs `railway redeploy --from-source` (a plain `railway redeploy` reruns the image
already deployed), then `make railway-version` once the new deployment is up, to confirm it reports the new version.

Run `make release-check` on its own to see what's still missing. If a release stops partway, finish it with the
remaining targets, since `release-check` refuses a version that is already on Docker Hub.

## License

Apache License 2.0. See `LICENSE.txt`.

## Environment Variables

| Variable         | Default               | Purpose                                                                  |
|------------------|-----------------------|--------------------------------------------------------------------------|
| `PORT`           | 8080                  | HTTP server port                                                         |
| `PREFIX`         | http://localhost:8080 | URL prefix for generated links; production sets `https://www.srcref.com` |
| `DEFAULT_BRANCH` | master                | Default GitHub branch                                                    |
| `MAX_CACHE_SIZE` | 2048                  | Max cached file entries                                                  |
| `MAX_LENGTH`     | 5MB                   | Max file size to process                                                 |

## Deployment

Multi-arch (amd64/arm64) Docker images are pushed by `make release` (see Releasing); `make build-docker` builds a
local single-arch image. Image runs on the Eclipse Temurin 25 JRE Alpine base image (`eclipse-temurin:25-jre-alpine`),
pinned by digest; its Java version must match `jre` in `libs.versions.toml`. Deployment itself is manual — no workflow
publishes the Docker image or deploys the app.

## Continuous Integration

Five GitHub Actions workflows in `.github/workflows/`:

| Workflow                            | Triggers                                                               | What it does                                                                |
|-------------------------------------|------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| `tests.yml` (Tests)                 | Push to `master`/`main`, any pull request, `workflow_dispatch`         | Runs `./gradlew check koverXmlReport`, uploads coverage to Codecov          |
| `docs.yml` (Documentation)          | Push to `master`/`main`, `workflow_dispatch`                           | Builds Dokka KDoc + the Zensical site, deploys both to GitHub Pages         |
| `zizmor.yml` (zizmor)               | Push to `master`/`main`, any pull request, `workflow_dispatch`         | Audits workflows and `dependabot.yml` with zizmor, uploads to code scanning |
| `security.yml` (Security)           | Push to `master`/`main`, any pull request, weekly, `workflow_dispatch` | Trivy scans the Docker image; CodeQL analyzes the Kotlin code               |
| `cache-cleanup.yml` (Cache Cleanup) | Weekly, after Security; `workflow_dispatch`                            | Deletes old Trivy database caches on `master` and closed PRs' caches        |

All run on `ubuntu-latest`. Tests, Documentation, and Security use Temurin JDK 17 and `gradle/actions/setup-gradle`; the
test task itself runs on the JDK 25 toolchain, which Gradle downloads through foojay (see Dependencies). `docs.yml`
copies `build/dokka/html` into `website/srcref/site/api-docs` so the KDoc is published under `/api-docs/`.
The site's theme override, `website/srcref/overrides/main.html` (set as `custom_dir` in `zensical.toml`), extends
`base.html` to add the Context7 chat widget (`data-library="/pambrose/srcref"`) to every page.

The Security workflow's `image-scan` job builds the fat JAR and the image (`srcref:scan`), then runs
`aquasecurity/trivy-action` on it, failing on a HIGH or CRITICAL vulnerability that has a fix, in either the Temurin
base layer or a library in the fat JAR. It runs weekly as well as on changes, since a newly published CVE can affect
code that hasn't changed. On non-PR runs it also uploads the HIGH and CRITICAL findings, unfixed ones included, as
SARIF to code scanning; PRs only print the table, since fork and Dependabot PRs lack the token to upload. The `codeql`
job compiles with Gradle's caches and daemon off and the Kotlin compiler in-process, so CodeQL sees every source file;
it is skipped on fork and Dependabot PRs. Because `make release-check` requires every check run on HEAD to pass, a
failing scan, including one from the weekly run on an unchanged `master`, blocks a release until it is fixed.
trivy-action caches about 1 GB of vulnerability database under a new `cache-trivy-YYYY-MM-DD` key each day it scans,
and a PR that scans saves its own copy under its PR ref, where only that PR can read it. Each Monday, after the
Security run, `cache-cleanup.yml` deletes every Trivy database on `master` but the newest, and every cache (Trivy and
Gradle) of a closed PR, so they don't push `master`'s Gradle caches out of GitHub's 10 GB limit.

`make ci` runs the build steps of the Tests, Documentation, and zizmor workflows locally, one after another, on the
working tree (uncommitted changes included): `./gradlew --rerun-tasks check koverXmlReport`, the Dokka + Zensical site
build with the KDoc copied in, and `make zizmor`. It skips the Codecov upload and the Pages deploy, and does not run the
Security workflow. It differs from CI in two ways: it builds the site with the zensical version locked in
`website/uv.lock` (`uv run`), whereas `docs.yml` runs `pip install zensical` and gets the latest release; and a zizmor
finding fails `make ci`, where the workflow only uploads it.

The zizmor workflow uses `zizmorcore/zizmor-action` in its default Advanced Security mode: findings are uploaded as
SARIF and appear in the repository's Security tab and as PR annotations, rather than failing the zizmor job itself.
Run `make zizmor` locally before pushing workflow changes. The local run is offline-only unless `GH_TOKEN` is set,
while CI also runs zizmor's online audits (e.g. impostor commits, known-vulnerable actions).

Workflow conventions that keep zizmor clean:

- Pin every action to a full commit SHA with a trailing `# vX.Y.Z` comment; Dependabot updates both.
- Set `persist-credentials: false` on `actions/checkout`.
- Declare an explicit `permissions:` block (least privilege) in every workflow.

Notes:

- Pushing a feature branch runs nothing on its own; Tests, zizmor, and Security run on the branch only once it has an
  open PR.
- No workflow has path filters, so every merge to `master` redeploys the docs site.
- Manually dispatching Documentation from a non-default branch will likely fail at the deploy step, since the
  `github-pages` environment normally only accepts deploys from the default branch.

### Dependabot

`.github/dependabot.yml` opens weekly update PRs for four ecosystems: `gradle` (the version catalog), `github-actions`,
`uv` (`website/`), and `docker` (the `Dockerfile` base image). Gradle minor/patch bumps are grouped into one PR, with
majors opened individually; Actions and uv updates are each grouped into a single PR. Dependabot PRs trigger the Tests,
zizmor, and Security workflows like any other PR, though Security skips its CodeQL job on them.

- Every ecosystem has a 7-day `cooldown`, so Dependabot only proposes releases at least a week old (zizmor's
  `dependabot-cooldown` audit requires this). Security updates are not delayed by the cooldown.
- The `gradle-wrapper` dependency is ignored: bump `gradle-wrapper` in `libs.versions.toml` and run
  `make upgrade-wrapper` instead, so the jar and `gradlew` scripts are regenerated along with the properties file.
- The base image is pinned as `eclipse-temurin:25-jre-alpine@sha256:…`, and Dependabot bumps the digest as the tag is
  rebuilt with Alpine and JRE fixes. Java majors are ignored: moving to a new one means changing the tag and `jre` in
  `libs.versions.toml` together.
- A Kotlin bump PR does not update the Kotlin version badge in `README.md`; fix that by hand.
