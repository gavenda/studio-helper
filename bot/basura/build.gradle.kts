plugins {
    id("bogus.kotlin-conventions")
    id("bogus.bot-application")
}

version = "2.2"

application {
    mainClass.set("bogus.bot.basura.AppKt")
}

dependencies {
    implementation(libs.bundles.log4j2)
    implementation(projects.common)
    implementation(projects.library.database)
    implementation(projects.extension.about)
    implementation(projects.extension.aniradio)
    implementation(projects.extension.anilist)
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-basura")
    }
}
