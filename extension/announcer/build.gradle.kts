plugins {
    id("bogus.kordex")
}

version = "1.0"

dependencies {
    implementation(project(":common"))
    implementation("com.sedmelluq:lavaplayer:1.3.78")
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-announcer")
    }
}