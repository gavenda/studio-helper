import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(Compiler.JVM_TARGET))
    }

    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = Compiler.JVM_TARGET.toString()
        }
    }
}
