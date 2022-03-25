plugins {
    id("bogus.kordex")
}

version = "1.0"

dependencies {
    implementation(project(":common"))
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-about")
    }
}