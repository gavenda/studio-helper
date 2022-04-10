plugins {
    id("bogus.kordex-bot")
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
