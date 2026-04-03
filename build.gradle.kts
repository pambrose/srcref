import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  alias(libs.plugins.kotlin)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.ktor)
  alias(libs.plugins.pambrose.envvar)
  alias(libs.plugins.pambrose.stable.versions)
  alias(libs.plugins.pambrose.kotlinter)
  alias(libs.plugins.pambrose.snapshot)
  alias(libs.plugins.pambrose.testing)
  alias(libs.plugins.dokka)
  alias(libs.plugins.maven.publish)
}

// Change the version in README.md as well
version = "2.0.5"
group = "com.pambrose"
val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

buildConfig {
  buildConfigField("String", "NAME", "\"${project.name}\"")
  buildConfigField("String", "VERSION", "\"${project.version}\"")
  buildConfigField("String", "RELEASE_DATE", "\"${LocalDate.now().format(formatter)}\"")
  buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
}

application {
  mainClass = "com.pambrose.srcref.Main"
}

dependencies {
  implementation(libs.kotlin.coroutines)

  implementation(platform(libs.ktor.bom))
  implementation(libs.bundles.ktor)

//  implementation(platform(libs.utils.bom))
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

ktor {
  fatJar {
    archiveFileName.set("srcref-all.jar")
  }
}

dokka {
  moduleName.set("srcref")
  pluginsConfiguration.html {
    homepageLink.set("https://github.com/pambrose/srcref")
    footerMessage.set("srcref")
  }
}

mavenPublishing {
  configure(com.vanniktech.maven.publish.KotlinJvm(
    javadocJar = com.vanniktech.maven.publish.JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
    sourcesJar = true,
  ))
  coordinates("com.pambrose", "srcref", version.toString())

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

  publishToMavenCentral()
  signAllPublications()
}

