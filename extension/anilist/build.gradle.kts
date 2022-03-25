plugins {
    id("bogus.kordex")
}

version = "1.0"

dependencies {
    implementation(project(":common"))

    // Copy down
    implementation("io.github.furstenheim:copy_down:1.0")

    // DB
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:8.4.3")
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.ktorm:ktorm-support-postgresql:3.4.1")
    runtimeOnly("org.postgresql:postgresql:42.3.1")
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-anilist")
    }
}