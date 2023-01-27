plugins {
    id("bogus.kotlin-conventions")
}

version = "1.0"

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kordex)
    api(libs.bundles.metrics)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-library-metrics")
    }
}