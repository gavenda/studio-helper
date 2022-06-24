plugins {
    id("bogus.kotlin-conventions")
    id("bogus.bot-application")
}

version = "2.2"

application {
    mainClass.set("bogus.bot.basura.AppKt")
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kordex)
    implementation(libs.bundles.log4j2)

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
