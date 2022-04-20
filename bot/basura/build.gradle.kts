plugins {
    id("bogus.kordex")
    id("bogus.bot")
    id("bogus.logging")
}

version = "2.2"

application {
    mainClass.set("bogus.bot.basura.AppKt")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":extension:about"))
    implementation(project(":extension:aniradio"))
    implementation(project(":extension:anilist"))
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-basura")
    }
}
