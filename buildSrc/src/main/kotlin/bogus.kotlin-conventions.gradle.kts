import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Compiler.JVM_TARGET))
    }

    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.10"))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = Compiler.JVM_TARGET.toString()
        }
    }
}
