VERSION=$(shell grep '^version' build.gradle.kts | head -1 | sed 's/.*"\(.*\)"/\1/')

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

trigger-build:
	curl -s "https://jitpack.io/com/github/pambrose/srcref/${VERSION}/build.log"

view-build:
	curl -s "https://jitpack.io/api/builds/com.github.pambrose/srcref/${VERSION}" | python3 -m json.tool

dist:
	./gradlew installDist

stage:
	./gradlew stage

purge:
	 heroku builds:cache:purge -a srcref --confirm srcref

versioncheck:
	./gradlew dependencyUpdates --no-configuration-cache

upgrade-wrapper:
	./gradlew wrapper --gradle-version=9.2.0 --distribution-type=bin
