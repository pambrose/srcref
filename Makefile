VERSION=1.0.4

default: versioncheck

build-all: clean stage

clean:
	./gradlew clean

compile: build

build:
	./gradlew build -xtest

tests:
	./gradlew --rerun-tasks check

uberjar:
	./gradlew uberjar

uber: uberjar
	java -jar build/libs/srcref.jar

run-docker:
	docker run --rm --env-file=docker_env_vars -p 8080:8080 pambrose/srcref:${VERSION}

build-docker:
	docker build -t pambrose/srcref:${VERSION} .

run-docker:
	docker run --rm --env-file=docker_env_vars -p 8080:8080 pambrose/srcref:${VERSION}

PLATFORMS := linux/amd64
IMAGE_PREFIX := pambrose/srcref

docker-push:
	# prepare multiarch
	docker buildx use buildx 2>/dev/null || docker buildx create --use --name=buildx
	docker buildx build --platform ${PLATFORMS} --push -t ${IMAGE_PREFIX}:latest -t ${IMAGE_PREFIX}:${VERSION} .

release: clean build uberjar docker-push


dist:
	./gradlew installDist

stage:
	./gradlew stage

purge:
	 heroku builds:cache:purge -a srcref --confirm srcref

versioncheck:
	./gradlew dependencyUpdates

upgrade-wrapper:
	./gradlew wrapper --gradle-version=7.4.2 --distribution-type=bin