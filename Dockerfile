# The tests run on this Java version, set by `jre` in gradle/libs.versions.toml; change the two together. Dependabot
# bumps the digest as the tag moves, but never the Java version.
FROM eclipse-temurin:25-jre-alpine@sha256:56f273059de6b13a6b49fe0ded691ed2d0399bbee76156a8f2d385f7524a3d43
LABEL maintainer="Paul Ambrose <pambrose@mac.com>"

# The base image predates Alpine 3.24's libexpat 2.10.0-r0, which fixes four HIGH CVEs in the 2.9.0-r0 it ships
# (CVE-2026-113351, CVE-2026-115506, CVE-2026-117610 and CVE-2026-118370). Upgrade only that package, and fail the build
# rather than ship an older one. Drop this once a new base digest carries the fix.
RUN apk add --no-cache --upgrade 'libexpat>=2.10.0-r0'

# Define the user to use in this instance to prevent using root that even in a container, can be a security risk.
ENV APPLICATION_USER=srcref

# Then add the user, create the /app folder and give permissions to our user.
RUN adduser --disabled-password --gecos '' $APPLICATION_USER
RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

# Mark this container to use the specified $APPLICATION_USER
USER $APPLICATION_USER

COPY ./build/libs/srcref-all.jar /app/srcref-all.jar
COPY src/main/resources /app/src/main/resources

# Make /app the working directory
WORKDIR /app

EXPOSE 8080

CMD []
# Launch java to execute the jar with defaults intended for containers. --enable-native-access lets Jansi load its
# native library without the warning that a future Java release will block it.
ENTRYPOINT ["java", "-server", "-Xmx2048m", "--enable-native-access=ALL-UNNAMED", "-Dlogback.configurationFile=/app/src/main/resources/logback-srcref.xml", "-jar", "/app/srcref-all.jar"]
