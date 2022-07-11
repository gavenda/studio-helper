plugins {
    id("bogus.kotlin-conventions")
}

version = "1.0"

dependencies {
    implementation(projects.common)
    implementation(projects.library.database)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-moderation")
    }
}