plugins {
    id("bogus.kordex-bot")
    id("org.hidetake.ssh") version "2.10.1"
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

apply(from = "ssh.gradle")

tasks {
    getByName("deploy") {
        dependsOn(getByName("installDist"))
    }
}