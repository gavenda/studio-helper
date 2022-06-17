plugins {
    alias(libs.plugins.kotlin.jvm)
}

version = "1.0"

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(project(":common"))
    implementation(libs.kordex)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-automove")
    }
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(11))
    }
}