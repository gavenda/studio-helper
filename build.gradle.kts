plugins {
    base
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}