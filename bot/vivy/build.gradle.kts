plugins {
    id("bogus.kotlin-conventions")
    id("bogus.bot-application")
}

version = "2.6"

application {
    mainClass.set("bogus.bot.vivy.AppKt")
}

dependencies {
    implementation(libs.bundles.log4j2)
    implementation(projects.common)
    implementation(projects.library.database)
    implementation(projects.library.metrics)
    implementation(projects.extension.about)
    implementation(projects.extension.music)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-vivy")
    }
}
