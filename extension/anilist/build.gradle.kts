plugins {
    id("bogus.kotlin-conventions")
    alias(libs.plugins.kotlin.serialization)
}

version = "1.0"

dependencies {
    implementation(projects.common)
    implementation(projects.library.database)
    implementation(libs.copy.down)
    implementation(libs.krontab)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-anilist")
    }
}
