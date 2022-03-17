plugins {
    id("bogus.kordex")
}

repositories {
    maven {
        name = "DRSchlaubi Releases"
        url = uri("https://schlaubi.jfrog.io/artifactory/lavakord")
    }
}

dependencies {
    implementation(project(":common"))
    implementation("dev.schlaubi.lavakord:kord:3.5.1")
    implementation("de.sonallux.spotify:spotify-web-api-java:2.4.0")

    // DB
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:8.4.3")
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.ktorm:ktorm-support-postgresql:3.4.1")
    runtimeOnly("org.postgresql:postgresql:42.3.1")
}