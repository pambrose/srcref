import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SourcesJar
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  alias(libs.plugins.kotlin)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.ktor)
  alias(libs.plugins.pambrose.kotlinter)
  alias(libs.plugins.pambrose.testing)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kover)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.detekt)
  alias(libs.plugins.ben.manes.versions)
}

// Version and group are defined in gradle.properties; also update version refs in README.md and website/srcref/docs/{api,getting-started}.md
providers.gradleProperty("overrideVersion").orNull?.let { version = it }

val projectUrl = "https://github.com/pambrose/srcref"
val detektConfigDir = "$rootDir/config/detekt"
val jvmTargetVersion = libs.versions.jvm.get()

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

configureKotlin()
configureKtor()
configureDetekt()
configureDokka()
configurePublishing()
configureVersions()

fun Project.configureKotlin() {
  kotlin {
    jvmToolchain(jvmTargetVersion.toInt())

    sourceSets.all {
      listOf(
        "kotlin.time.ExperimentalTime",
        "kotlinx.coroutines.DelicateCoroutinesApi",
        "kotlin.concurrent.atomics.ExperimentalAtomicApi",
      ).forEach { languageSettings.optIn(it) }
    }
  }

  // Run the unused-return-value checker over production code only. Kotest's
  // assertion DSL (e.g. shouldBe) returns its receiver, and tests intentionally
  // discard that result, so applying the checker to the test source set would
  // emit only false-positive warnings.
  tasks.named<KotlinCompile>("compileKotlin") {
    compilerOptions {
      freeCompilerArgs.add("-Xreturn-value-checker=check")
    }
  }
}

fun Project.configureKtor() {
  ktor {
    fatJar {
      archiveFileName.set("${project.name}-all.jar")
    }
  }
}

fun Project.configureDetekt() {
  detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$detektConfigDir/detekt.yml"))
    baseline = file("$detektConfigDir/detekt-baseline.xml")
    buildUponDefaultConfig = true
  }

  tasks.withType<Detekt>().configureEach {
    jvmTarget.set(jvmTargetVersion)
    reports {
      html.required.set(false)
      checkstyle.required.set(false)
    }
  }

  tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget.set(jvmTargetVersion)
  }
}

fun Project.configureDokka() {
  dokka {
    moduleName.set(project.name)
    pluginsConfiguration.html {
      homepageLink.set(projectUrl)
      footerMessage.set(project.name)
    }
  }
}

fun Project.configurePublishing() {
  mavenPublishing {
    configure(
      KotlinJvm(
        javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
        sourcesJar = SourcesJar.Sources(),
      ),
    )

    pom {
      name.set(project.name)
      description.set("Dynamic Line-Specific GitHub Permalinks")
      url.set(projectUrl)
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
        url.set(projectUrl)
      }
    }

    publishToMavenCentral(automaticRelease = true)
    // Skip signing when no GPG key is provided (e.g., local publishing)
    if (project.findProperty("signingInMemoryKey") != null) {
      signAllPublications()
    }
  }
}

fun Project.configureVersions() {
  // A pre-release qualifier is a `.` or `-` delimiter followed by a known unstable
  // keyword. `m\d` matches milestones (`-M1`/`.M2`) without catching stable classifiers
  // like `-macos`/`-MR1`, and the `[.-]` delimiter catches both dash-style (`-alpha`)
  // and dot-style (Netty's `.Beta1`) qualifiers while leaving `-jre`/`.Final` stable.
  val preReleaseQualifier =
    Regex("""[.-](rc|beta|alpha|m\d|cr|snapshot|eap|dev|milestone|pre)""", RegexOption.IGNORE_CASE)

  fun isNonStable(version: String): Boolean = preReleaseQualifier.containsMatchIn(version)

  tasks.withType<DependencyUpdatesTask>().configureEach {
    notCompatibleWithConfigurationCache("the dependency updates plugin is not compatible with the configuration cache")
    // Reject a pre-release candidate only when the current version is stable. For
    // dependencies we intentionally track on a pre-release line (e.g. a detekt
    // alpha), newer pre-releases are still surfaced as available updates.
    rejectVersionIf {
      isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
  }
}

