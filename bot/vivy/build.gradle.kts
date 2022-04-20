plugins {
    id("bogus.kordex")
    id("bogus.bot")
    id("bogus.logging")
}

version = "2.6"

application {
    mainClass.set("bogus.bot.vivy.AppKt")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":extension:music"))
    implementation(project(":extension:about"))
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-vivy")
    }
}
