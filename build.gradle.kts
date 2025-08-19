import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  java
  application
  `maven-publish`
  alias(libs.plugins.kotlin)
  alias(libs.plugins.kotlinter)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.versions)
  alias(libs.plugins.shadow)
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      groupId = "com.github.pambrose.srcref"
      artifactId = "srcref"
      version = "1.9.7"

      from(components["java"])
    }
  }
}

group = "com.github.pambrose.srcref"
// Change version in Makefile and README.md as well
version = "1.9.7"
val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

buildConfig {
  buildConfigField("String", "NAME", "\"${project.name}\"")
  buildConfigField("String", "VERSION", "\"${project.version}\"")
  buildConfigField("String", "RELEASE_DATE", "\"${LocalDate.now().format(formatter)}\"")
  buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
}

val mainName = "com.pambrose.srcref.Main"

application {
  mainClass = mainName
}

repositories {
  google()
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  implementation(libs.kotlin.coroutines)

  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.cio)
  implementation(libs.ktor.server.html)

  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.cio)

  implementation(libs.kotlin.css)

  implementation(libs.utils.ktor.server)
  implementation(libs.utils.core)

  implementation(libs.dropwizard.core)
  implementation(libs.dropwizard.jvm)

  implementation(libs.commons.text)

  implementation(libs.logback)
  implementation(libs.kotlin.logging)

  testImplementation(libs.kotest)
}

kotlin {
  jvmToolchain(17)

  sourceSets.all {
    languageSettings.optIn("kotlin.time.ExperimentalTime")
    languageSettings.optIn("kotlinx.coroutines.DelicateCoroutinesApi")
    languageSettings.optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
  }
}

tasks.test {
  useJUnitPlatform()

  testLogging {
    events("passed", "skipped", "failed", "standardOut", "standardError")
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    showStandardStreams = true
  }
}

tasks.register("stage") {
  dependsOn("uberjar", "build", "clean")
}

tasks.named("build") {
  mustRunAfter("clean")
}

val uberjar by tasks.registering(Jar::class) {
  dependsOn(tasks.shadowJar)
  archiveFileName.set("srcref.jar")
  manifest {
    attributes("Main-Class" to mainName)
  }
  from(zipTree(tasks.shadowJar.get().archiveFile))
}

//tasks.shadowJar {
//  isZip64 = true
//  mergeServiceFiles()
//  exclude("META-INF/*.SF")
//  exclude("META-INF/*.DSA")
//  exclude("META-INF/*.RSA")
//  exclude("LICENSE*")
//}

tasks.register<Jar>("sourcesJar") {
  dependsOn("classes")
  from(sourceSets.main.get().allSource)
  archiveClassifier.set("sources")
}

kotlinter {
  reporters = arrayOf("checkstyle", "plain")
}
