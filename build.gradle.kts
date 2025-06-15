import dev.kordex.gradle.plugins.kordex.DataCollection
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"

    id("com.gradleup.shadow") version "8.3.6"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.10"
    id("dev.kordex.gradle.kordex") version "1.6.1"
}

group = "org.britmoji"
version = "1.0.0"

val main = "org.britmoji.yoodle.AppKt"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Logging
    implementation("io.github.oshai:kotlin-logging:7.0.7")
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")

    // SVG -> PNG Conversion Feature
    implementation("org.apache.xmlgraphics:batik-transcoder:1.19")
    implementation("org.apache.xmlgraphics:batik-codec:1.19")
}

kordEx {
    bot {
        dataCollection(DataCollection.None)
        mainClass = main
    }
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