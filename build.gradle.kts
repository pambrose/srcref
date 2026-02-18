import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  alias(libs.plugins.kotlin)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.ktor)
  alias(libs.plugins.pambrose.envvar)
  alias(libs.plugins.pambrose.stable.versions)
  alias(libs.plugins.pambrose.kotlinter)
  alias(libs.plugins.pambrose.repos)
  alias(libs.plugins.pambrose.snapshot)
  alias(libs.plugins.pambrose.testing)
}

// Change version in Makefile and README.md as well
version = "2.0.1"
group = "com.github.pambrose.srcref"
val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

buildConfig {
  buildConfigField("String", "NAME", "\"${project.name}\"")
  buildConfigField("String", "VERSION", "\"${project.version}\"")
  buildConfigField("String", "RELEASE_DATE", "\"${LocalDate.now().format(formatter)}\"")
  buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
}

dependencies {
  implementation(libs.kotlin.coroutines)

  implementation(platform(libs.ktor.bom))
  implementation(libs.bundles.ktor)

  implementation(platform(libs.utils.bom))
  implementation(libs.bundles.common.utils)

  implementation(platform(libs.dropwizard.bom))
  implementation(libs.bundles.dropwizard)

  implementation(libs.commons.text)

  implementation(libs.logback)
  implementation(libs.kotlin.logging)

  testImplementation(libs.kotest)
  testImplementation(libs.ktor.server.test.host)
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

application {
  mainClass = "com.pambrose.srcref.Main"
}

ktor {
  fatJar {
    archiveFileName.set("srcref-all.jar")
  }
}
