plugins {
    id("bogus.kotlin-conventions")
    alias(libs.plugins.kotlin.serialization)
}

version = "1.0"

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(project(":common"))
    implementation(libs.kordex)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-counter")
    }
}
