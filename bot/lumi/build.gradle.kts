plugins {
    id("bogus.kotlin-conventions")
    id("bogus.bot-application")
}

version = "1.0"

application {
    mainClass.set("bogus.bot.lumi.AppKt")
}

dependencies {
    implementation(libs.bundles.log4j2)
    implementation(projects.common)
    implementation(projects.library.database)
    implementation(projects.extension.about)
    implementation(projects.extension.administration)
    implementation(projects.extension.counter)
    implementation(projects.extension.moderation)
    implementation(projects.extension.utility)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-lumi")
    }
}
