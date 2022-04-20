plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:8.5.8")
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.ktorm:ktorm-support-postgresql:3.4.1")
    runtimeOnly("org.postgresql:postgresql:42.3.4")
}