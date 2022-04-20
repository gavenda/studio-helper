plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin(module = "gradle-plugin", version = "1.6.21"))
    implementation(kotlin(module = "serialization", version = "1.6.21"))
}
