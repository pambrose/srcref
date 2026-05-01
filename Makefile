VERSION=$(shell grep '^version=' gradle.properties | head -1 | cut -d= -f2)

default: versioncheck

build-all: clean stage

stop:
	./gradlew --stop

clean:
	./gradlew clean

build:	clean
	./gradlew build -PreleaseDate=04/25/2026 -xtest

local-build: clean
	./gradlew build -PuseMavenLocal=true -PreleaseDate=04/25/2026 -xtest

tests:
	./gradlew --rerun-tasks check

local-tests:
	./gradlew --rerun-tasks -PuseMavenLocal=true check

run:
	./gradlew run

refresh:
	./gradlew --refresh-dependencies

fatjar: build
	./gradlew buildFatJar

uber: uberjar
	java -jar build/libs/srcref-all.jar

run-docker:
	docker run --rm --env-file=docker_env_vars -p 8080:8080 pambrose/srcref:${VERSION}

build-docker:
	docker build -t pambrose/srcref:${VERSION} .

PLATFORMS := linux/amd64,linux/arm64/v8
IMAGE_NAME := pambrose/srcref

docker-push:
	# prepare multiarch
	docker buildx use buildx 2>/dev/null || docker buildx create --use --name=buildx
	docker buildx build --platform ${PLATFORMS} --push -t ${IMAGE_NAME}:latest -t ${IMAGE_NAME}:${VERSION} .

release: clean build build-docker docker-push

deploy:
	./secrets/deploy-app.sh
	say finished app deployment

do-log:
	./secrets/app-log.sh

dist:
	./gradlew installDist

stage:
	./gradlew stage

purge:
	 heroku builds:cache:purge -a srcref --confirm srcref

versioncheck:
	./gradlew dependencyUpdates --no-configuration-cache

kdocs:
	./gradlew dokkaGeneratePublicationHtml

coverage:
	./gradlew koverHtmlReport

coverage-xml:
	./gradlew koverXmlReport

coverage-verify:
	./gradlew koverVerify

clean-docs:
	rm -rf website/srcref/site
	rm -rf website/srcref/.cache

site: clean-docs
	cd website/srcref && uv run zensical serve

publish-local:
	./gradlew publishToMavenLocal

publish-local-snapshot:
	./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenLocal

GPG_ENV = \
	ORG_GRADLE_PROJECT_signingInMemoryKey="$$(gpg --armor --export-secret-keys $$GPG_SIGNING_KEY_ID)" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=$$(security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w)

check-gpg-env:
	@if [ -z "$$GPG_SIGNING_KEY_ID" ]; then \
		echo "Error: GPG_SIGNING_KEY_ID is not set" >&2; exit 1; \
	fi
	@if ! gpg --list-secret-keys "$$GPG_SIGNING_KEY_ID" >/dev/null 2>&1; then \
		echo "Error: no GPG secret key found for GPG_SIGNING_KEY_ID=$$GPG_SIGNING_KEY_ID" >&2; exit 1; \
	fi
	@if ! security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w >/dev/null 2>&1; then \
		echo "Error: keychain entry 'gradle-signing-password' (account 'gpg-signing') not found" >&2; exit 1; \
	fi

publish-snapshot: check-gpg-env
	$(GPG_ENV) ./gradlew -PoverrideVersion=$(VERSION)-SNAPSHOT publishToMavenCentral

publish-maven-central: check-gpg-env
	$(GPG_ENV) ./gradlew publishAndReleaseToMavenCentral

upgrade-wrapper:
	./gradlew wrapper --gradle-version=9.5.0 --distribution-type=bin
