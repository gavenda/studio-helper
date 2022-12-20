plugins {
    id("bogus.kotlin-conventions")
    id("bogus.bot-application")
}

version = "1.0"

application {
    mainClass.set("bogus.bot.parrot.AppKt")
}

dependencies {
    implementation(libs.bundles.log4j2)
    implementation(projects.common)
    implementation(projects.extension.announcer)
    implementation(projects.extension.automove)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-parrot")
    }
}
