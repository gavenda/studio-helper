plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin(module = "gradle-plugin", version = "1.6.20"))
    implementation(kotlin(module = "serialization", version = "1.6.20"))
}
