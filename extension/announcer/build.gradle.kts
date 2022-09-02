plugins {
    id("bogus.kotlin-conventions")
    alias(libs.plugins.kotlin.serialization)
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
    implementation(libs.krontab)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-announcer")
    }
}
