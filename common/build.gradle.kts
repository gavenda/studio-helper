plugins {
    id("bogus.kotlin-conventions")
}

version = "1.0"

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)

    api(libs.kordex)
    api(libs.kord)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-common")
    }
}
