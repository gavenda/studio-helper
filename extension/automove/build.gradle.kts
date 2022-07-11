plugins {
    id("bogus.kotlin-conventions")
}

version = "1.0"

dependencies {
    implementation(projects.common)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-automove")
    }
}
