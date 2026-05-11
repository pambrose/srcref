.PHONY: default help build-all stop clean build tests run refresh \
	fatjar uber run-docker build-docker docker-push release deploy do-log dist stage \
	purge versioncheck kdocs coverage coverage-html coverage-xml coverage-log \
	coverage-open coverage-packages coverage-clean coverage-verify clean-docs site \
	publish-local publish-local-snapshot publish-snapshot publish-maven-central \
	upgrade-wrapper lint detekt detekt-baseline \
	_check-gpg-env _require-version _require-gradle-version

# Strip inline `# comment` text and surrounding whitespace so trailing notes don't poison the value.
VERSION=$(shell awk -F= '/^[[:space:]]*version[[:space:]]*=/ {sub(/#.*/,"",$$2); gsub(/[[:space:]]/,"",$$2); print $$2; exit}' gradle.properties)
GRADLE_VERSION=$(shell awk -F'"' '/^[[:space:]]*gradle[[:space:]]*=/ {print $$2; exit}' gradle/libs.versions.toml)

PLATFORMS := linux/amd64,linux/arm64/v8
IMAGE_NAME := pambrose/srcref

GPG_ENV := \
	ORG_GRADLE_PROJECT_signingInMemoryKey="$$(gpg --armor --export-secret-keys $$GPG_SIGNING_KEY_ID)" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyId="$$GPG_SIGNING_KEY_ID" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=$$(security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w)

default: versioncheck

help:  ## Show this help (list of targets)
	@awk 'BEGIN {FS = ":.*?## "; printf "Usage: make <target>\n\nTargets:\n"} \
		/^[a-zA-Z0-9_-]+:.*?## / {printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2}' \
		$(MAKEFILE_LIST)

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
	./gradlew build -xtest

lint:  ## Run kotlinter and detekt
	./gradlew lintKotlin detekt

detekt:  ## Run detekt static analysis
	./gradlew detekt

detekt-baseline:  ## Regenerate the detekt baseline
	./gradlew detektBaseline

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

run-docker: _require-version ## Run the published Docker image locally
	docker run --rm --env-file=docker_env_vars -p 8080:8080 pambrose/srcref:$(VERSION)

build-docker: _require-version build  ## Build the Docker image
	docker build -t pambrose/srcref:$(VERSION) .

docker-push: _require-version  ## Build and push multi-arch Docker image
	# buildx rebuilds for both architectures, so a prior single-arch `build-docker` would be wasted work.
	docker buildx use buildx 2>/dev/null || docker buildx create --use --name=buildx
	docker buildx build --platform $(PLATFORMS) --push -t $(IMAGE_NAME):latest -t $(IMAGE_NAME):$(VERSION) .

release: docker-push  ## Build and push release Docker images

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

versioncheck:  ## Check for outdated dependencies
	# --no-configuration-cache: the gradle-versions plugin (`dependencyUpdates`) is not config-cache compatible.
	./gradlew dependencyUpdates --no-configuration-cache

kdocs:  ## Generate KDoc HTML documentation
	./gradlew dokkaGeneratePublicationHtml

clean-docs:  ## Remove generated docs site
	rm -rf website/srcref/site
	rm -rf website/srcref/.cache

site: clean-docs  ## Serve the docs site locally
	cd website/srcref && uv run zensical serve

publish-local: _require-version  ## Publish to local Maven repo (~/.m2)
	./gradlew publishToMavenLocal

publish-local-snapshot: _require-version  ## Publish a SNAPSHOT to local Maven repo
	./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenLocal

publish-snapshot: _require-version _check-gpg-env  ## Publish a SNAPSHOT to Maven Central
	$(GPG_ENV) ./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenCentral

publish-maven-central: _require-version _check-gpg-env  ## Publish and release to Maven Central
	$(GPG_ENV) ./gradlew publishAndReleaseToMavenCentral

upgrade-wrapper: _require-gradle-version  ## Upgrade the Gradle wrapper to the version in libs.versions.toml
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin

_check-gpg-env:
	@if [ -z "$$GPG_SIGNING_KEY_ID" ]; then \
		echo "Error: GPG_SIGNING_KEY_ID is not set" >&2; exit 1; \
	fi
	@if ! gpg --list-secret-keys "$$GPG_SIGNING_KEY_ID" >/dev/null 2>&1; then \
		echo "Error: no GPG secret key found for GPG_SIGNING_KEY_ID=$$GPG_SIGNING_KEY_ID" >&2; exit 1; \
	fi
	@if ! security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w >/dev/null 2>&1; then \
		echo "Error: keychain entry 'gradle-signing-password' (account 'gpg-signing') not found" >&2; exit 1; \
	fi

_require-version:
	@[ -n "$(VERSION)" ] || { echo "ERROR: Could not determine project version from gradle.properties" >&2; exit 1; }

_require-gradle-version:
	@[ -n "$(GRADLE_VERSION)" ] || { echo "ERROR: Could not determine gradle version from gradle/libs.versions.toml" >&2; exit 1; }
