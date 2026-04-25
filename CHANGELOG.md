# Changelog

All notable changes to this project. Format is loosely based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [2.0.9](https://github.com/pambrose/srcref/releases/tag/2.0.9) — 2026-04-24

- Bump version to 2.0.9
- Bump dependencies: Kotlin 2.3.21, Ktor 3.4.3, common-utils 2.8.1
- Move `repositories {}` to `settings.gradle.kts` via `dependencyResolutionManagement` with `FAIL_ON_PROJECT_REPOS`
- Gate `mavenLocal()` behind `-PuseMavenLocal=true` for opt-in local-artifact resolution
- Add `local-build`, `local-tests`, and `fatjar` Makefile targets
- Replace `.map` with `.forEach` for the Kotlin opt-in language settings loop
- Import `KotlinJvm` instead of using fully-qualified name in `mavenPublishing` configuration

**Full Changelog**: https://github.com/pambrose/srcref/compare/2.0.8...2.0.9

## [2.0.8](https://github.com/pambrose/srcref/releases/tag/2.0.8) — 2026-04-16

- Configure Gradle daemon JVM memory to fix metaspace exhaustion ([#32](https://github.com/pambrose/srcref/pull/32))
- Bump to 2.0.8, add license headers, reorganize tests, and enhance docs site ([#33](https://github.com/pambrose/srcref/pull/33))
- Remove navigation tabs from docs site config ([#34](https://github.com/pambrose/srcref/pull/34))
- Bump dependencies, align test packages, and enrich CallLogging ([#35](https://github.com/pambrose/srcref/pull/35))

**Full Changelog**: https://github.com/pambrose/srcref/compare/2.0.7...2.0.8

## [2.0.7](https://github.com/pambrose/srcref/releases/tag/2.0.7) — 2026-04-04

- Fix KDocs 404 caused by path conflict with Zensical output (#23)
- Fix KDocs link to use absolute URL (#24)
- Move non-Kotlin snippets from .kt to .txt files (#25)
- Bump version to 2.0.6, fix Makefile paths and add signing/snapshot targets (#26)
- Add explicit versions to BOM-managed dependencies for Maven publishing (#27)
- Bump to 2.0.7 and support overrideVersion for snapshot publishing (#28)
- Replace deprecated maven-publish API and clean up Urls.kt (#29)
- Extract GPG_ENV variable in Makefile and skip signing without GPG key (#30)
- Migrate common-utils from JitPack to Maven Central and bump dependencies (#31)

## [2.0.5](https://github.com/pambrose/srcref/releases/tag/2.0.5) — 2026-04-03

### Documentation Website
- Added a full [Zensical](https://zensical.org/)-based documentation site with 7 pages: Getting Started, Query Parameters, Regex Guide, Programmatic API, Advanced Usage, Deployment, and KDocs
- Code examples live in `src/test/kotlin/website/` and are compile-checked by Gradle, imported into docs via pymdownx.snippets
- Added KDocs page linking to Dokka-generated API reference

### CI/CD
- Added GitHub Actions workflow for running tests and linting on pushes and pull requests (`.github/workflows/tests.yml`)
- Added GitHub Actions workflow for building and deploying the documentation site with Dokka KDocs to GitHub Pages (`.github/workflows/docs.yml`)

### Publishing
- Switched from JitPack to Maven Central publishing via vanniktech maven-publish plugin
- Added Dokka javadoc generation and GPG signing
- Added Apache License 2.0

### Build Updates
- Updated Gradle wrapper to 9.4.1
- Updated dependencies: Kotest 6.1.10, Ktor 3.4.2, gradle-plugins 1.0.11, utils 2.6.4
- Added KDoc comments across all source files
- Added Dokka configuration for HTML documentation
- Added documentation link to the srcref edit page header

### Cleanup
- Removed unused `SrcRefDslTag` DSL marker annotation
- Cleaned up jitpack.yml and Makefile targets

## [2.0.4](https://github.com/pambrose/srcref/releases/tag/2.0.4) — 2026-03-17

- Update Kotlin to 2.3.20
- Bump dependency versions (gradle-plugins 1.0.10, kotest 6.1.7, utils 2.6.2)

## [2.0.3](https://github.com/pambrose/srcref/releases/tag/2.0.3) — 2026-03-04

* Update jars

## [2.0.2](https://github.com/pambrose/srcref/releases/tag/2.0.2) — 2026-03-01

_No release notes._

## [2.0.1](https://github.com/pambrose/srcref/releases/tag/2.0.1) — 2026-03-01

* 2.0.1 by @pambrose in https://github.com/pambrose/srcref/pull/15

## [2.0.0](https://github.com/pambrose/srcref/releases/tag/2.0.0) — 2026-02-13

* Cleaned up with Claude Code
* Updated jars

## [1.9.7](https://github.com/pambrose/srcref/releases/tag/1.9.7) — 2025-06-26

* Update jars
* Update to Kotlin 2.2.0

## [1.9.6](https://github.com/pambrose/srcref/releases/tag/1.9.6) — 2025-03-23

* Update jars

## [1.9.5](https://github.com/pambrose/srcref/releases/tag/1.9.5) — 2025-03-21

* Update jars

## [1.9.2](https://github.com/pambrose/srcref/releases/tag/1.9.2) — 2024-12-20

* Update jars

## [1.9.1](https://github.com/pambrose/srcref/releases/tag/1.9.1) — 2024-12-11

* Update jars

## [1.8.0](https://github.com/pambrose/srcref/releases/tag/1.8.0) — 2024-10-19

* Update jars

## [1.7.0](https://github.com/pambrose/srcref/releases/tag/1.7.0) — 2024-10-18

* Update jars

## [1.6.0](https://github.com/pambrose/srcref/releases/tag/1.6.0) — 2024-06-11

* Update jars

## [1.4.0](https://github.com/pambrose/srcref/releases/tag/1.4.0) — 2024-01-07

Upgrade to Kotlin 1.9.22

## [1.3.0](https://github.com/pambrose/srcref/releases/tag/1.3.0) — 2023-12-12

* Add threaddump support
* Upgrade to kotlin 1.9.21
* Upgrade dependencies

## [1.2.0](https://github.com/pambrose/srcref/releases/tag/1.2.0) — 2023-11-02

* Upgrade to kotlin 1.9.20
* Update jars

## [1.1.0](https://github.com/pambrose/srcref/releases/tag/1.1.0) — 2023-07-12

* Update to kotlin 1.9.0

## [1.0.25](https://github.com/pambrose/srcref/releases/tag/1.0.25) — 2023-05-16

Update jars

## [1.0.24](https://github.com/pambrose/srcref/releases/tag/1.0.24) — 2023-05-07

Update jars

## [1.0.23](https://github.com/pambrose/srcref/releases/tag/1.0.23) — 2023-04-11

* Adjust for jitpack.io build

## [1.0.22](https://github.com/pambrose/srcref/releases/tag/1.0.22) — 2023-04-11

* Misc cleanup for jitpack.io jar publiching

## [1.0.21](https://github.com/pambrose/srcref/releases/tag/1.0.21) — 2023-04-11

* Fix maven publish for jitpack.io jar

## [1.0.20](https://github.com/pambrose/srcref/releases/tag/1.0.20) — 2023-04-10

* Upgrade logger

## [1.0.19](https://github.com/pambrose/srcref/releases/tag/1.0.19) — 2023-04-10

* Update to Kotlin 1.8.20

## [1.0.18](https://github.com/pambrose/srcref/releases/tag/1.0.18) — 2023-01-01

* Update to Kotlin 1.8.0

## [1.0.17](https://github.com/pambrose/srcref/releases/tag/1.0.17) — 2022-11-20

* Update jars

## [1.0.15](https://github.com/pambrose/srcref/releases/tag/1.0.15) — 2022-10-19

* Revert back to /error endpoint

## [1.0.14](https://github.com/pambrose/srcref/releases/tag/1.0.14) — 2022-10-17

* Change /error endpoint to /exception

## [1.0.13](https://github.com/pambrose/srcref/releases/tag/1.0.13) — 2022-10-12

* Update jars
* Add empty robots.txt endpoint

## [1.0.12](https://github.com/pambrose/srcref/releases/tag/1.0.12) — 2022-10-03

* Fix reference count on the initial request

## [1.0.11](https://github.com/pambrose/srcref/releases/tag/1.0.11) — 2022-10-03

* Update jars

## [1.0.10](https://github.com/pambrose/srcref/releases/tag/1.0.10) — 2022-09-24

* Update jars

## [1.0.9](https://github.com/pambrose/srcref/releases/tag/1.0.9) — 2022-08-27

* Upgrade jars

## [1.0.8](https://github.com/pambrose/srcref/releases/tag/1.0.8) — 2022-06-11

* Upgrade to kotlin 1.7.0

## [1.0.7](https://github.com/pambrose/srcref/releases/tag/1.0.7) — 2022-06-05

* Fix compiler stack overflow issue

## [1.0.6](https://github.com/pambrose/srcref/releases/tag/1.0.6) — 2022-06-05

* Add banner to server start
* Redirect to www for root tld requests

## [1.0.5](https://github.com/pambrose/srcref/releases/tag/1.0.5) — 2022-05-30

* Add tooltip support
* Remove ping calls from the log
* Moved Url endpoints into an enum
* Ignore end query params if end regex is missing

## [1.0.4](https://github.com/pambrose/srcref/releases/tag/1.0.4) — 2022-05-30

* Add support for content caching

## [1.0.3](https://github.com/pambrose/srcref/releases/tag/1.0.3) — 2022-05-25

* Add support for line ranges
* Improve error handling

## [1.0.2](https://github.com/pambrose/srcref/releases/tag/1.0.2) — 2022-05-24

* Adjust srcRefUrl() params

## [1.0.1](https://github.com/pambrose/srcref/releases/tag/1.0.1) — 2022-05-24

* Add encode option to srcrefUrl()

## [1.0.0](https://github.com/pambrose/srcref/releases/tag/1.0.0) — 2022-05-24

* Initial release

