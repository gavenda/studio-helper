plugins {
    id("bogus.kotlin-conventions")
}

version = "1.0"

dependencies {
    implementation(projects.common)
    implementation(projects.library.database)
    implementation(libs.kord) {
        capabilities {
            requireCapability(KordCapability.VOICE)
        }
    }
    implementation(LAVAPLAYER_NATIVE_EXTRAS)
    implementation(libs.lavaplayer.fork)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-aniradio")
    }
}
