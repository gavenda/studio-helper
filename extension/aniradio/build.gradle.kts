plugins {
    id("bogus.common")
    id("bogus.kordex")
    id("bogus.player")
}

version = "1.0"

dependencies {
    implementation(project(":common"))
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-aniradio")
    }
}