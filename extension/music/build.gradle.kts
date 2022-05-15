plugins {
    id("bogus.common")
    id("bogus.common-db")
    id("bogus.kordex")
}

version = "2.0"

dependencies {
    implementation(project(":common"))
    implementation("dev.schlaubi.lavakord:kord:3.6.2")
    implementation("com.sedmelluq:lavaplayer:1.3.78")
    implementation("com.github.natanbc:lavadsp:0.7.7")
    implementation("de.sonallux.spotify:spotify-web-api-java:2.4.0")
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-music")
    }
}