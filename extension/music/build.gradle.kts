plugins {
    id("bogus.kordex")
}

version = "1.0"

dependencies {
    implementation(project(":common"))
    implementation("dev.schlaubi.lavakord:kord:3.6.0")
    implementation("de.sonallux.spotify:spotify-web-api-java:2.4.0")

    // DB
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:8.5.8")
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.ktorm:ktorm-support-postgresql:3.4.1")
    runtimeOnly("org.postgresql:postgresql:42.3.4")
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-music")
    }
}