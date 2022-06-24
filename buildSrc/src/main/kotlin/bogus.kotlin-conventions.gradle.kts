import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }

    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = Compiler.JVM_TARGET
        }
    }
}
