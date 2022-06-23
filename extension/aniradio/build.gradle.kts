plugins {
    alias(libs.plugins.kotlin.jvm)
}

version = "1.0"

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(project(":common"))
    implementation(libs.kordex)
    implementation(libs.kord) {
        capabilities {
            requireCapability("dev.kord:core-voice")
        }
    }
    implementation(files("$rootDir/lib/lavaplayer-natives-extra-1.3.13.jar"))
    implementation(libs.lavaplayer.fork)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-extension-aniradio")
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }

    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
    }
}