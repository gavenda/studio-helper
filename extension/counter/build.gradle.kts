plugins {
    kotlin("plugin.serialization")
    id("bogus.kordex")
}

dependencies {
    implementation(project(":common"))
}