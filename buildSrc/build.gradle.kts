plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.6.10"))
    implementation(kotlin("serialization", version = "1.6.10"))
}
