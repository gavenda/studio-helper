plugins {
    kotlin("jvm")
}

dependencies {
    implementation("dev.kord:kord-core") {
        capabilities {
            requireCapability("dev.kord:core-voice")
        }
    }
    implementation(files("$rootDir/lib/lavaplayer-natives-extra-1.3.13.jar"))
    implementation("com.github.walkyst:lavaplayer-fork:1.3.97.1")
}
