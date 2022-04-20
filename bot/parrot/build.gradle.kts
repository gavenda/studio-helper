plugins {
    id("bogus.kordex")
    id("bogus.bot")
    id("bogus.logging")
}

version = "1.0"

application {
    mainClass.set("bogus.bot.parrot.AppKt")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":extension:announcer"))
    implementation(project(":extension:automove"))
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-parrot")
    }
}
