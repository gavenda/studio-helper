plugins {
    application
    id("bogus.common")
}

dependencies {
    implementation(project(":core"))

    // Logging
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
}
