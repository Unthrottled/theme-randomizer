import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** Get a property from the gradle.properties file. */
fun properties(key: String) = providers.gradleProperty(key).get()

/**
 * Returns the value of the environment variable associated with the
 * specified key.
 *
 * @param key the key of the environment variable
 * @return the value of the environment variable as a Provider<String>
 */
fun environment(key: String) = providers.environmentVariable(key)

/** Get a property from a file. */
fun fileProperties(key: String) = project.findProperty(key).toString().let { if (it.isNotEmpty()) file(it) else null }

plugins {
  // Java support
  id("java")
  alias(libs.plugins.kotlin)
  alias(libs.plugins.gradleIntelliJPlugin)
  alias(libs.plugins.changelog)
  alias(libs.plugins.qodana)
  alias(libs.plugins.detekt)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.kover)
}

// Import variables from gradle.properties file
val pluginGroup: String by project
val pluginName: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

// Configure project's dependencies
repositories {
  mavenCentral()
  mavenLocal()
  gradlePluginPortal()

  intellijPlatform {
    defaultRepositories()
    jetbrainsRuntime()
  }
}

dependencies {
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
  implementation("commons-io:commons-io:2.11.0")
  implementation("io.sentry:sentry:6.18.1")
  testImplementation("org.assertj:assertj-core:3.24.2")
  testImplementation("io.mockk:mockk:1.13.5")

  intellijPlatform {
    intellijIdeaUltimate(platformVersion, useInstaller = false)
    instrumentationTools()
    pluginVerifier()
    zipSigner()

    bundledPlugins(
      "com.intellij.java",
    )
  }
}

kotlin {
  jvmToolchain(17)
}

configurations {
  implementation.configure {
    exclude("org.slf4j")
  }
}

intellijPlatform {
  pluginConfiguration {
    id = pluginGroup
    name = pluginName
    version = pluginVersion

    description = File("./README.md").readText().lines().run {
      val start = "<!-- Plugin description -->"
      val end = "<!-- Plugin description end -->"

      if (!containsAll(listOf(start, end))) {
        throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
      }
      subList(indexOf(start) + 1, indexOf(end))
    }.joinToString("\n").run { markdownToHTML(this) }

    ideaVersion {
      sinceBuild = pluginSinceBuild
      untilBuild = pluginUntilBuild
    }

    changeNotes = provider {
      markdownToHTML(File("./docs/RELEASE-NOTES.md").readText())
    }
  }

  publishing {
    token = environment("PUBLISH_TOKEN")
    channels = listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
  }

  signing {
    certificateChain = environment("CERTIFICATE_CHAIN")
    privateKey = environment("PRIVATE_KEY")
    password = environment("PRIVATE_KEY_PASSWORD")
  }
}

detekt {
  config.setFrom("./detekt-config.yml")
  buildUponDefaultConfig = true
  autoCorrect = true
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
    options.compilerArgs = listOf("-Xlint:deprecation", "-Xlint:unchecked")
  }

  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  withType<Detekt> {
    jvmTarget = "17"
  }

  buildSearchableOptions {
    enabled = false
  }

  wrapper {
    gradleVersion = properties("gradleVersion")
  }

  runIde {
    maxHeapSize = "2g"
  }
}
