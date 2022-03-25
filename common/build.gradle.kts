plugins {
    id("bogus.common")
    id("bogus.kordex")
}

version = "1.0"

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-common")
    }
}