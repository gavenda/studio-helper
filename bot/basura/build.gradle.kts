plugins {
    id("bogus.kordex-bot")
    id("org.hidetake.ssh") version "2.10.1"
}

version = "2.2"

application {
    mainClass.set("bogus.bot.basura.AppKt")
}

dependencies {
    implementation(project(":extension:about"))
    implementation(project(":extension:anilist"))
}

apply(from = "ssh.gradle")

tasks {
    getByName("deploy") {
        dependsOn(getByName("installDist"))
    }
}