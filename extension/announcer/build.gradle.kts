plugins {
    id("bogus.kotlin-conventions")
    alias(libs.plugins.kotlin.serialization)
}

version = "1.0"

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(project(":common"))
    implementation(libs.kordex)
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
        archiveBaseName.set("bogus-extension-announcer")
    }
}
