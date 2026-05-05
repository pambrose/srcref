import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SourcesJar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  alias(libs.plugins.kotlin)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.ktor)
  alias(libs.plugins.pambrose.stable.versions)
  alias(libs.plugins.pambrose.kotlinter)
  alias(libs.plugins.pambrose.testing)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kover)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.detekt)
}

// Version and group are defined in gradle.properties; also update version refs in README.md and website/srcref/docs/{api,getting-started}.md
providers.gradleProperty("overrideVersion").orNull?.let { version = it }

val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
val releaseDate = providers.gradleProperty("releaseDate").orNull ?: LocalDate.now().format(formatter)
val buildTime = providers.gradleProperty("buildTime").orNull?.toLong() ?: System.currentTimeMillis()

buildConfig {
  buildConfigField("String", "NAME", "\"${project.name}\"")
  buildConfigField("String", "VERSION", "\"${project.version}\"")
  buildConfigField("String", "RELEASE_DATE", "\"$releaseDate\"")
  buildConfigField("long", "BUILD_TIME", "${buildTime}L")
}

application {
  mainClass = "com.pambrose.srcref.Main"
}

dependencies {
  implementation(libs.kotlin.coroutines)
  implementation(libs.bundles.ktor)
  implementation(libs.bundles.common.utils)
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
    listOf(
      "kotlin.time.ExperimentalTime",
      "kotlinx.coroutines.DelicateCoroutinesApi",
      "kotlin.concurrent.atomics.ExperimentalAtomicApi",
    ).forEach { languageSettings.optIn(it) }
  }
}

ktor {
  fatJar {
    archiveFileName.set("srcref-all.jar")
  }
}

val jvmTargetVersion = "17"

detekt {
  toolVersion = libs.versions.detekt.get()
  config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
  baseline = file("$rootDir/config/detekt/detekt-baseline.xml")
  buildUponDefaultConfig = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
  jvmTarget = jvmTargetVersion
  reports {
    html.required.set(false)
    xml.required.set(false)
  }
}

tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
  jvmTarget = jvmTargetVersion
}

dokka {
  moduleName.set("srcref")
  pluginsConfiguration.html {
    homepageLink.set("https://github.com/pambrose/srcref")
    footerMessage.set("srcref")
  }
}

mavenPublishing {
  configure(
    KotlinJvm(
      javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
      sourcesJar = SourcesJar.Sources(),
    ),
  )

  pom {
    name.set("srcref")
    description.set("Dynamic Line-Specific GitHub Permalinks")
    url.set("https://github.com/pambrose/srcref")
    licenses {
      license {
        name.set("Apache License 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0")
      }
    }
    developers {
      developer {
        id.set("pambrose")
        name.set("Paul Ambrose")
        email.set("pambrose@srcref.com")
      }
    }
    scm {
      connection.set("scm:git:git://github.com/pambrose/srcref.git")
      developerConnection.set("scm:git:ssh://github.com/pambrose/srcref.git")
      url.set("https://github.com/pambrose/srcref")
    }
  }

  publishToMavenCentral(automaticRelease = true)
  // Skip signing when no GPG key is provided (e.g., local publishing)
  if (project.findProperty("signingInMemoryKey") != null) {
    signAllPublications()
  }
}
