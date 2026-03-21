VERSION=$(shell grep '^version =' build.gradle.kts | head -1 | sed 's/.*"\(.*\)"/\1/')
JITPACK_BUILD_LOG := https://jitpack.io/com/github/pambrose/srcref/$(VERSION)/build.log
JITPACK_BUILD_API := https://jitpack.io/api/builds/com.github.pambrose/srcref/$(VERSION)

default: versioncheck

build-all: clean stage

stop:
	./gradlew --stop

clean:
	./gradlew clean

build:	clean
	./gradlew build -x test

run:
	./gradlew run

refresh:
	./gradlew --refresh-dependencies

tests:
	./gradlew --rerun-tasks check

uber: build
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

trigger-jitpack:
	until curl -s "$(JITPACK_BUILD_LOG)" | grep -qv "not found"; do \
		echo "Waiting for JitPack..."; \
		sleep 10; \
	done
	echo "JitPack build triggered for version ${VERSION}"

view-jitpack:
	curl -s "$(JITPACK_BUILD_LOG)"
	curl -s "$(JITPACK_BUILD_API)" | jq

dist:
	./gradlew installDist

stage:
	./gradlew stage

purge:
	 heroku builds:cache:purge -a srcref --confirm srcref

versioncheck:
	./gradlew dependencyUpdates --no-configuration-cache

upgrade-wrapper:
	./gradlew wrapper --gradle-version=9.4.1 --distribution-type=bin
