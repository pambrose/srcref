.PHONY: default build-all stop clean build local-build tests local-tests run refresh \
	fatjar uber run-docker build-docker docker-push release deploy do-log dist stage \
	purge versioncheck kdocs coverage coverage-xml coverage-verify clean-docs site \
	publish-local publish-local-snapshot check-gpg-env publish-snapshot \
	publish-maven-central upgrade-wrapper lint detekt-baseline

VERSION=$(shell awk -F= '/^version[[:space:]]*=/ {gsub(/[[:space:]]/,"",$$2); print $$2; exit}' gradle.properties)
GRADLE_VERSION=$(shell awk -F'"' '/^gradle[[:space:]]*=/ {print $$2; exit}' gradle/libs.versions.toml)

default: versioncheck

.NOTPARALLEL: build-all release

build-all: clean stage

stop:
	./gradlew --stop

clean:
	./gradlew clean

build:
	./gradlew build -xtest

lint:
	./gradlew lintKotlin detekt

detekt-baseline:
	./gradlew detektBaseline

coverage: coverage-html coverage-xml

coverage-html:
	./gradlew koverHtmlReport

coverage-xml:
	./gradlew koverXmlReport

coverage-log:
	./gradlew koverLog

coverage-verify:
	./gradlew koverVerify

coverage-open: coverage-html
	open build/reports/kover/html/index.html

coverage-packages: coverage-xml
	@python3 -c "import xml.etree.ElementTree as ET; \
r = ET.parse('build/reports/kover/report.xml').getroot(); \
pkgs = []; \
[pkgs.append((p.get('name'), int(c.get('covered')), int(c.get('missed')))) \
 for p in r.findall('package') for c in p.findall('counter') if c.get('type') == 'INSTRUCTION']; \
pkgs.sort(key=lambda x: -x[2]); \
print(f\"{'package':<55} {'cov%':>6} {'covered':>9} {'missed':>9} {'total':>9}\"); \
[print(f'{n:<55} {(c/(c+m)*100 if c+m else 0):6.1f} {c:9d} {m:9d} {c+m:9d}') for n,c,m in pkgs]; \
tc=sum(p[1] for p in pkgs); tm=sum(p[2] for p in pkgs); \
print(f'\nOVERALL: {tc/(tc+tm)*100:.2f}% ({tc}/{tc+tm} instructions, {tm} missed)')"

coverage-clean:
	./gradlew cleanAllTests
	rm -rf build/reports/kover build/kover

tests:
	./gradlew --rerun-tasks check

run:
	./gradlew run

refresh:
	./gradlew --refresh-dependencies

fatjar: build
	./gradlew buildFatJar

uber: fatjar
	java -jar build/libs/srcref-all.jar

run-docker:
	docker run --rm --env-file=docker_env_vars -p 8080:8080 pambrose/srcref:$(VERSION)

build-docker: build
	docker build -t pambrose/srcref:$(VERSION) .

PLATFORMS := linux/amd64,linux/arm64/v8
IMAGE_NAME := pambrose/srcref

docker-push: build-docker
	# prepare multiarch
	docker buildx use buildx 2>/dev/null || docker buildx create --use --name=buildx
	docker buildx build --platform $(PLATFORMS) --push -t $(IMAGE_NAME):latest -t $(IMAGE_NAME):$(VERSION) .

release: docker-push

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
	ORG_GRADLE_PROJECT_signingInMemoryKeyId="$$GPG_SIGNING_KEY_ID" \
	ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="$$(security find-generic-password -a "gpg-signing" -s "gradle-signing-password" -w)"

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
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin
