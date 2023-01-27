plugins {
    id("bogus.kotlin-conventions")
}

version = "1.0"

dependencies {
    api(libs.kotlin.stdlib)
    api(libs.kotlin.reflect)
    api(libs.kordex)
    api(libs.kord)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-common")
    }
}
