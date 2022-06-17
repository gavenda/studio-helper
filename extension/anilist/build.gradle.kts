plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

version = "1.0"

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.bundles.database)
    runtimeOnly(libs.postgresql)

    implementation(project(":common"))
    implementation(libs.kordex)
    implementation("io.github.furstenheim:copy_down:1.0")
    implementation("dev.inmo:krontab:0.7.2")
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-anilist")
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}