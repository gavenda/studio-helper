import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            name = "Sonatype Snapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
        maven {
            name = "Dv8tion"
            url = uri("https://m2.dv8tion.net/releases")
        }
        maven {
            name = "Kotlin Discord"
            url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
        }
        maven {
            name = "DRSchlaubi Releases"
            url = uri("https://schlaubi.jfrog.io/artifactory/lavakord")
        }
        maven {
            name = "JCenter"
            url = uri("https://jcenter.bintray.com/")
        }
        maven {
            name = "Jitpack"
            url = uri("https://jitpack.io/")
        }
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = listOf(
                    "-opt-in=kotlin.RequiresOptIn"
                )
            }
        }
    }
}