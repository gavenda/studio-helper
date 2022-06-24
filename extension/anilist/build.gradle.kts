plugins {
    id("bogus.kotlin-conventions")
    alias(libs.plugins.kotlin.serialization)
}

version = "1.0"

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.bundles.database)
    runtimeOnly(libs.postgresql)

    implementation(project(":common"))
    implementation(libs.kordex)
    implementation(libs.copy.down)
    implementation(libs.krontab)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-anilist")
    }
}
