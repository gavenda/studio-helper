plugins {
    id("bogus.common")
    id("bogus.kordex")
}

version = "1.0"

dependencies {
    implementation(project(":common"))
    implementation("dev.kord:kord-core") {
        capabilities {
            requireCapability("dev.kord:core-voice")
        }
    }
    implementation("com.github.walkyst:lavaplayer-fork:custom-SNAPSHOT")
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-aniradio")
    }
}