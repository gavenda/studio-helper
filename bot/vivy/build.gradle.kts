plugins {
    id("bogus.kotlin-conventions")
    id("bogus.bot-application")
}

version = "2.6"

application {
    mainClass.set("bogus.bot.vivy.AppKt")
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kordex)
    implementation(libs.bundles.log4j2)

    implementation(project(":common"))
    implementation(project(":extension:music"))
    implementation(project(":extension:about"))
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-vivy")
    }
}
