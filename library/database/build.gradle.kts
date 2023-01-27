plugins {
    id("bogus.kotlin-conventions")
}

version = "1.0"

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kordex)
    api(libs.bundles.database) {
        exclude("org.postgresql")
    }
    runtimeOnly(libs.postgresql)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-library-database")
    }
}