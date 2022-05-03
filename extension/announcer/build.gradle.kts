plugins {
    id("bogus.common")
    id("bogus.kordex")
    kotlin("plugin.serialization")
}

version = "1.0"

dependencies {
    implementation(project(":common"))
    implementation(files("lib/lavaplayer-natives-extra-1.3.13.jar"))
    implementation("com.sedmelluq:lavaplayer:1.3.78")
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-announcer")
    }
}