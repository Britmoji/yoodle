import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.20-Beta1"
    kotlin("plugin.serialization") version "2.0.20-Beta1"

    id("io.github.goooler.shadow") version "8.1.8"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.6"
}

group = "org.britmoji"
version = "1.0.0"

val main = "org.britmoji.yoodle.AppKt"

repositories {
    mavenCentral()
    maven("https://jitpack.io")

    maven {
        name = "Sonatype Snapshots (Legacy)"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        name = "Sonatype Snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // Kord
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.9.0-SNAPSHOT")

    // Logging
    implementation("io.github.microutils:kotlin-logging:3.0.4")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")

    // SVG -> PNG Conversion Feature
    implementation("org.apache.xmlgraphics:batik-transcoder:1.16")
    implementation("org.apache.xmlgraphics:batik-codec:1.16")
}

// Java build
tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xopt-in=kotlin.RequiresOptIn")
    }
}

tasks.jar {
    manifest.attributes(
        "Main-Class" to main
    )
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks["build"].dependsOn(tasks.shadowJar)

// IDEA
idea {
    project {
        settings {
            runConfigurations {
                create<org.jetbrains.gradle.ext.Application>("Run Yoodle") {
                    mainClass = main
                    moduleName = "yoodle.main"

                    val workDir = rootProject.file("run").also { it.mkdirs() }
                    workingDirectory = workDir.absolutePath
                }
            }
        }
    }
}