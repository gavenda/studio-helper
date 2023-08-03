plugins {
    id("bogus.kotlin-conventions")
}

version = "2.0"

dependencies {
    implementation(projects.common)
    implementation(projects.library.database)
    implementation(projects.library.metrics)
    implementation(libs.kord.voice)
    implementation(LAVAPLAYER_NATIVE_EXTRAS)
    implementation(libs.lavaplayer.fork)
    implementation(libs.lavakord) {
        exclude("dev.kord")
    }
    implementation(libs.lavadsp)
    implementation(libs.spotify)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-music")
    }
}
