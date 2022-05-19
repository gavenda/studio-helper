plugins {
    id("bogus.common")
    id("bogus.kordex")
    kotlin("plugin.serialization")
}

version = "1.0"

dependencies {
    implementation(project(":common"))
    implementation("com.github.walkyst:lavaplayer-fork:custom-SNAPSHOT")
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-announcer")
    }
}