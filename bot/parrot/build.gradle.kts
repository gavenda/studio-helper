plugins {
    id("bogus.kordex-bot")
}

version = "1.0"

application {
    mainClass.set("bogus.bot.basura.AppKt")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":extension:announcer"))
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-parrot")
    }
}
