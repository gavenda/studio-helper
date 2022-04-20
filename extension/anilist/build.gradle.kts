plugins {
    id("bogus.common")
    id("bogus.common-db")
    id("bogus.kordex")
    kotlin("plugin.serialization")
}

version = "1.0"

dependencies {
    implementation(project(":common"))
    implementation("io.github.furstenheim:copy_down:1.0")
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-anilist")
    }
}