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
//  alias(libs.plugins.shadow)
  alias(libs.plugins.ktor.plugin)
}

// Change version in Makefile and README.md as well
version = "1.9.7"
group = "com.github.pambrose.srcref"
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
  implementation(libs.kotlin.css)

  implementation(platform(libs.ktor.bom))
  implementation(libs.bundles.ktor)

  implementation(platform(libs.utils.bom))
  implementation(libs.bundles.common.utils)

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

java {
  withSourcesJar()
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

tasks.test {
  useJUnitPlatform()

  testLogging {
    events("passed", "skipped", "failed", "standardOut", "standardError")
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    showStandardStreams = true
  }
}

//tasks.register("stage") {
//  dependsOn("uberjar", "build", "clean")
//}

tasks.named("build") {
  mustRunAfter("clean")
}

//tasks.register<Jar>("sourcesJar") {
//  dependsOn("classes")
//  from(sourceSets.main.get().allSource)
//  archiveClassifier.set("sources")
//}

kotlinter {
  reporters = arrayOf("checkstyle", "plain")
}
