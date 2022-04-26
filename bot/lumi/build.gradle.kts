plugins {
    id("bogus.kordex")
    id("bogus.bot")
    id("bogus.logging")
}

version = "1.0"

application {
    mainClass.set("bogus.bot.lumi.AppKt")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":extension:about"))
    implementation(project(":extension:moderation"))
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-lumi")
    }
}
