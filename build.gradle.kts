plugins {
    base
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(11))
    }
}