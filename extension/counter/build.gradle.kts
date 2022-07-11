plugins {
    id("bogus.kotlin-conventions")
    alias(libs.plugins.kotlin.serialization)
}

version = "1.0"

dependencies {
    implementation(projects.common)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-counter")
    }
}
