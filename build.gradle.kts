import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  kotlin ("jvm") version "2.2.20"
  application
  id("com.gradleup.shadow") version "9.2.2"
}

group = "me.ganorton"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven(url = "https://jitpack.io")
}

val vertxVersion = "5.0.7"
val junitJupiterVersion = "5.9.1"
val newpipeVersion = "v0.25.2"

val mainVerticleName = "me.ganorton.youpipe.MainVerticle"
val launcherClassName = "io.vertx.launcher.application.VertxApplication"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-launcher-application")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation("com.github.teamnewpipe:NewPipeExtractor:$newpipeVersion")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.fromTarget("17")
    languageVersion = KotlinVersion.fromVersion("2.0")
    apiVersion = KotlinVersion.fromVersion("2.0")
  }
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf(mainVerticleName)
}
