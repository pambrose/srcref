.PHONY: default help build-all stop clean build tests run refresh \
	fatjar uber run-docker build-docker docker-prebuild docker-push docker-verify \
	release release-check _confirm-release deploy do-log dist stage \
	purge versions versions-all kdocs coverage coverage-html coverage-xml coverage-log \
	coverage-open coverage-packages coverage-clean coverage-verify check-site upgrade-site clean-site site \
	publish-local publish-local-snapshot publish-snapshot publish-maven-central \
	upgrade-wrapper lint detekt detekt-baseline zizmor \
	_buildx _check-gpg-env _require-version _require-final-version _require-gradle-version

VERSION := $(shell sed -n 's/^version=\(.*\)/\1/p' gradle.properties)
GRADLE_VERSION := $(shell sed -n 's/^gradle-wrapper = "\(.*\)"/\1/p' gradle/libs.versions.toml)

# buildx records linux/arm64/v8 as linux/arm64 in the image index, so docker-verify looks for that form.
PLATFORMS := linux/amd64,linux/arm64
IMAGE_NAME := pambrose/srcref

# `make release` publishes only from a clean, up-to-date checkout of this branch whose GitHub CI has passed.
RELEASE_BRANCH ?= master
MAVEN_CENTRAL_URL := https://repo1.maven.org/maven2/com/pambrose/srcref
# The files "Version Management" in CLAUDE.md bumps to the new version; release-check fails if one doesn't mention
# VERSION. Keep the two lists in step.
RELEASE_VERSION_FILES := README.md website/srcref/docs/api.md website/srcref/docs/getting-started.md
# HEAD when make started. `release` passes it on to `_confirm-release`, which publishes nothing if HEAD has moved
# since release-check vetted it.
RELEASE_SHA := $(shell git rev-parse HEAD 2>/dev/null)

WEBSITE_DIR := website
SITE_DIR := $(WEBSITE_DIR)/srcref

GPG_ENV := \
	ORG_GRADLE_PROJECT_signingInMemoryKey="$$(gpg --armor --export-secret-keys $$GPG_SIGNING_KEY_ID)" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyId="$$GPG_SIGNING_KEY_ID" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=$$(security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w)

default: help

help:  ## Show this help (list of targets)
	@awk 'BEGIN {FS = ":.*?## "; printf "Usage: make <target>\n\nTargets:\n"} \
		/^[a-zA-Z0-9_-]+:.*?## / {printf "  \033[36m%-24s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# .NOTPARALLEL ignores its prerequisites — its presence forces the whole Makefile to run
# serially under -j. Recipes here mostly wrap Gradle (which manages its own parallelism),
# so serial top-level execution is what we want.
.NOTPARALLEL:

build-all: clean stage  ## Clean and stage a full build

stop:  ## Stop the Gradle daemon
	./gradlew --stop

clean:  ## Remove Gradle build outputs
	./gradlew clean

build:  ## Build without running tests
	./gradlew build -x test

lint:  ## Run kotlinter and detekt
	./gradlew lintKotlin detekt

detekt:  ## Run detekt static analysis
	./gradlew detekt

detekt-baseline:  ## Regenerate the detekt baseline
	./gradlew detektBaseline

zizmor:  ## Audit GitHub Actions workflows and Dependabot config with zizmor
	uvx zizmor .

coverage:  ## Generate HTML and XML coverage reports
	./gradlew koverHtmlReport koverXmlReport

coverage-html:  ## Generate HTML coverage report (Kover)
	./gradlew koverHtmlReport

coverage-xml:  ## Generate XML coverage report (Kover)
	./gradlew koverXmlReport

coverage-log:  ## Print Kover coverage log
	./gradlew koverLog

coverage-verify:  ## Verify coverage rules (Kover)
	./gradlew koverVerify

coverage-open: coverage-html  ## Open HTML coverage report in browser
	open build/reports/kover/html/index.html

coverage-packages: coverage-xml  ## Print per-package coverage summary
	@python3 scripts/coverage_packages.py

coverage-clean:  ## Clean all coverage artifacts
	./gradlew cleanAllTests
	rm -rf build/reports/kover build/kover

tests:  ## Run all tests and checks
	./gradlew --rerun-tasks check

run:  ## Run the dev server (port 8080)
	./gradlew run

refresh:  ## Refresh Gradle dependencies
	./gradlew --refresh-dependencies

fatjar: build  ## Build the fat JAR
	./gradlew buildFatJar

uber: fatjar  ## Build and run the fat JAR
	java -jar build/libs/srcref-all.jar

run-docker: _require-version  ## Run the published Docker image locally
	docker run --rm --env-file=docker_env_vars -p 8080:8080 pambrose/srcref:$(VERSION)

build-docker: _require-version build  ## Build the Docker image
	docker build -t pambrose/srcref:$(VERSION) .

# Builds every platform into the buildx builder's cache and pushes nothing, so `release` finds a broken image build
# before it publishes anything, and the docker-push after its prompt is mostly cache hits.
docker-prebuild: _require-version fatjar _buildx  ## Build the multi-arch Docker image without pushing it
	docker buildx build --platform $(PLATFORMS) --output type=cacheonly .

docker-push: _require-final-version fatjar _buildx  ## Build and push multi-arch Docker image
	# buildx rebuilds for both architectures, so a prior single-arch `build-docker` would be wasted work.
	docker buildx build --platform $(PLATFORMS) --push -t $(IMAGE_NAME):latest -t $(IMAGE_NAME):$(VERSION) .

docker-verify: _require-version  ## Check the published VERSION image has all PLATFORMS and latest matches it
	@out=$$(docker buildx imagetools inspect $(IMAGE_NAME):$(VERSION)) || exit 1; \
	for platform in $$(echo $(PLATFORMS) | tr ',' ' '); do \
		echo "$$out" | grep -q "Platform: *$$platform" \
			|| { echo "ERROR: $(IMAGE_NAME):$(VERSION) is missing $$platform" >&2; exit 1; }; \
	done; \
	latest=$$(docker buildx imagetools inspect $(IMAGE_NAME):latest | grep -m1 '^Digest:'); \
	[ "$$latest" = "$$(echo "$$out" | grep -m1 '^Digest:')" ] \
		|| { echo "ERROR: $(IMAGE_NAME):latest is not $(IMAGE_NAME):$(VERSION)" >&2; exit 1; }; \
	echo "$(IMAGE_NAME):$(VERSION) (and :latest) published for $(PLATFORMS)"

# Publishes nothing: it fails unless this checkout is fit to publish VERSION. The local checks run first, then the
# network ones (origin, GitHub's CI checks for HEAD, Docker Hub, Maven Central).
release-check: _require-final-version _check-gpg-env  ## Fail unless this checkout is fit to publish VERSION
	@test -z "$$(git status --porcelain)" \
		|| { echo "ERROR: the working tree is dirty; commit or stash first" >&2; exit 1; }
	@branch=$$(git rev-parse --abbrev-ref HEAD); [ "$$branch" = "$(RELEASE_BRANCH)" ] \
		|| { echo "ERROR: releases are cut from $(RELEASE_BRANCH), not $$branch" >&2; exit 1; }
	@grep -q '^## \[$(VERSION)\]' CHANGELOG.md \
		|| { echo "ERROR: CHANGELOG.md has no [$(VERSION)] section" >&2; exit 1; }
	@grep -q '^## \[v$(VERSION)\]' RELEASE_NOTES.md \
		|| { echo "ERROR: RELEASE_NOTES.md has no [v$(VERSION)] section" >&2; exit 1; }
	@missing=$$(for f in $(RELEASE_VERSION_FILES); do grep -qF '$(VERSION)' "$$f" || echo "  $$f"; done); \
	[ -z "$$missing" ] \
		|| { printf 'ERROR: not bumped to $(VERSION) (see Version Management in CLAUDE.md):\n%s\n' "$$missing" >&2; exit 1; }
	@docker info >/dev/null 2>&1 \
		|| { echo "ERROR: Docker isn't running; docker-push needs it" >&2; exit 1; }
	@git fetch -q origin $(RELEASE_BRANCH) \
		&& [ "$$(git rev-parse HEAD)" = "$$(git rev-parse origin/$(RELEASE_BRANCH))" ] \
		|| { echo "ERROR: HEAD is not origin/$(RELEASE_BRANCH); pull or push first" >&2; exit 1; }
	@tagged=$$(git rev-parse -q --verify "refs/tags/$(VERSION)^{commit}"); \
	[ -z "$$tagged" ] || [ "$$tagged" = "$$(git rev-parse HEAD)" ] \
		|| { echo "ERROR: tag $(VERSION) points at another commit" >&2; exit 1; }
	@runs=$$(gh api "repos/{owner}/{repo}/commits/$$(git rev-parse HEAD)/check-runs" --paginate \
		--jq '.check_runs[] | "\(.status) \(.conclusion) \(.name)"') \
		|| { echo "ERROR: could not read HEAD's CI checks from GitHub; is gh logged in?" >&2; exit 1; }; \
	[ -n "$$runs" ] || { echo "ERROR: no CI checks have run for HEAD" >&2; exit 1; }; \
	failed=$$(echo "$$runs" | grep -Ev '^completed (success|skipped|neutral) ' | sed 's/^/  /'); \
	[ -z "$$failed" ] || { printf 'ERROR: CI has not passed for HEAD:\n%s\n' "$$failed" >&2; exit 1; }
	@! docker buildx imagetools inspect $(IMAGE_NAME):$(VERSION) >/dev/null 2>&1 \
		|| { echo "ERROR: $(IMAGE_NAME):$(VERSION) is already on Docker Hub; bump the version" >&2; exit 1; }
	@! curl -fsI "$(MAVEN_CENTRAL_URL)/$(VERSION)/srcref-$(VERSION).pom" >/dev/null 2>&1 \
		|| { echo "ERROR: com.pambrose:srcref:$(VERSION) is already on Maven Central; bump the version" >&2; exit 1; }
	@echo "Ready to release $(VERSION) from $$(git rev-parse --short HEAD)"

# `release` passes RELEASE_SHA, the HEAD that release-check vetted and the tests and image build ran on.
_confirm-release:
	@[ "$$(git rev-parse HEAD)" = "$(RELEASE_SHA)" ] \
		|| { echo "ERROR: HEAD moved off $(RELEASE_SHA) during the release; nothing published" >&2; exit 1; }
	@test -z "$$(git status --porcelain)" \
		|| { echo "ERROR: the working tree changed during the release; nothing published" >&2; exit 1; }
	@echo "About to publish $(VERSION) from $$(git rev-parse --short HEAD). A Maven Central release can't be removed."
	@echo "  Docker Hub:    $(IMAGE_NAME), tagged $(VERSION) and latest, for $(PLATFORMS)"
	@echo "  Maven Central: com.pambrose:srcref:$(VERSION)"
	@printf 'Continue? [y/N] ' && read -r ans && [ "$$ans" = "y" ] || { echo "Nothing published."; exit 1; }

# Each step runs only if the one before it passed, so nothing is published until the checkout passes release-check,
# the tests, and a build of every image platform, and HEAD is still the commit those ran on. Docker Hub goes before
# Maven Central: its tags can be overwritten, while a Maven Central release can't be removed. A pushed tag also shows
# on Docker Hub at once, whereas repo1.maven.org can take a while to list a new release, so release-check's Docker Hub
# check is the one that stops a second run. After a partial release, finish with the remaining targets.
release:  ## Gate, test, build, then publish VERSION to Docker Hub and Maven Central (asks first)
	$(MAKE) release-check
	$(MAKE) tests
	$(MAKE) docker-prebuild
	@$(MAKE) --no-print-directory _confirm-release RELEASE_SHA=$(RELEASE_SHA)
	$(MAKE) docker-push
	$(MAKE) docker-verify
	$(MAKE) publish-maven-central
	@prev=$$(git describe --tags --abbrev=0 --exclude '$(VERSION)' 2>/dev/null); \
	echo; \
	echo "Published $(VERSION) to Docker Hub and Maven Central (repo1.maven.org can take a while to list it). Next:"; \
	echo "  1. Create the GitHub release (tag $(VERSION), title v$(VERSION)) with a summary that ends with"; \
	echo "     **Full Changelog**: https://github.com/pambrose/srcref/compare/$$prev...$(VERSION)"; \
	echo "     gh release create $(VERSION) --target $$(git rev-parse HEAD) --title v$(VERSION) --notes-file <notes>"; \
	echo "  2. make deploy"

deploy:  ## Deploy the app via secrets/deploy-app.sh
	./secrets/deploy-app.sh
	say finished app deployment

do-log:  ## Tail the deployed app log
	./secrets/app-log.sh

dist:  ## Install distribution via Gradle
	./gradlew installDist

stage:  ## Run the Gradle stage task
	./gradlew stage

purge:  ## Purge the Heroku build cache
	heroku builds:cache:purge -a srcref --confirm srcref

# dependencyUpdates is marked incompatible with the configuration cache (build.gradle.kts), so a run with the cache on
# builds an entry only to discard it, and says so. --no-configuration-cache skips the cache for this run instead, and
# --no-parallel overrides gradle.properties, since the plugin doesn't support parallel execution.
# build.gradle.kts gives the task a formatter that lists only what is behind; versions-all names the plugin's own plain
# report, which also lists everything up to date.
versions:  ## List the dependencies with newer versions, and any that could not be checked
	./gradlew dependencyUpdates --no-configuration-cache --no-parallel

versions-all:  ## The plugin's full dependency report, up-to-date dependencies included
	./gradlew dependencyUpdates --no-configuration-cache --no-parallel --output-formatter=plain

kdocs:  ## Generate KDoc HTML documentation
	./gradlew dokkaGeneratePublicationHtml

check-site:  ## Check for outdated website dependencies
	cd $(WEBSITE_DIR) && env -u VIRTUAL_ENV uv lock --upgrade --dry-run

upgrade-site:  ## Upgrade the website dependencies
	cd $(WEBSITE_DIR) && env -u VIRTUAL_ENV uv lock --upgrade

clean-site:  ## Remove generated docs site
	rm -rf $(SITE_DIR)/site
	rm -rf $(SITE_DIR)/.cache

site: clean-site  ## Serve the docs site locally
	cd $(SITE_DIR) && uv run zensical serve

publish-local: _require-version  ## Publish to local Maven repo (~/.m2)
	./gradlew publishToMavenLocal

publish-local-snapshot: _require-version  ## Publish a SNAPSHOT to local Maven repo
	./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenLocal

publish-snapshot: _require-version _check-gpg-env  ## Publish a SNAPSHOT to Maven Central
	$(GPG_ENV) ./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenCentral

publish-maven-central: _require-version _check-gpg-env  ## Publish and release to Maven Central
	$(GPG_ENV) ./gradlew publishAndReleaseToMavenCentral

# Gradle's documented upgrade procedure: the first run rewrites
# gradle-wrapper.properties using the *old* wrapper jar; the second run
# regenerates the wrapper itself with the new version.
upgrade-wrapper: _require-gradle-version  ## Upgrade the Gradle wrapper to the version in libs.versions.toml
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin

_check-gpg-env:
	@if [ -z "$$GPG_SIGNING_KEY_ID" ]; then \
		echo "Error: GPG_SIGNING_KEY_ID is not set" >&2; exit 1; \
	fi
	@if ! gpg --list-secret-keys "$$GPG_SIGNING_KEY_ID" >/dev/null 2>&1; then \
		echo "Error: no GPG secret key found for GPG_SIGNING_KEY_ID=$$GPG_SIGNING_KEY_ID" >&2; exit 1; \
	fi
	@if [ -z "$$(security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w 2>/dev/null)" ]; then \
		echo "Error: keychain entry 'gradle-signing-password' (account 'gpg-signing') is missing or empty" >&2; exit 1; \
	fi

_buildx:
	docker buildx use buildx 2>/dev/null || docker buildx create --use --name=buildx

_require-version:
	@[ -n "$(VERSION)" ] || { echo "ERROR: Could not determine project version from gradle.properties" >&2; exit 1; }

# Pre-release versions (any "-" suffix: -SNAPSHOT, -RC1, -M1, -beta...) are never released or pushed as :latest.
_require-final-version: _require-version
	@case "$(VERSION)" in *-*) \
		echo "ERROR: $(VERSION) is a pre-release; releases and :latest are for final versions" >&2; exit 1;; esac

_require-gradle-version:
	@[ -n "$(GRADLE_VERSION)" ] || { echo "ERROR: Could not determine gradle version from gradle/libs.versions.toml" >&2; exit 1; }
