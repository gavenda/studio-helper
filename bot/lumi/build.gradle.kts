plugins {
    id("bogus.kotlin-conventions")
    id("bogus.bot-application")
}

version = "1.0"

application {
    mainClass.set("bogus.bot.lumi.AppKt")
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kordex)
    implementation(libs.bundles.log4j2)

    implementation(project(":common"))
    implementation(project(":extension:about"))
    implementation(project(":extension:moderation"))
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-lumi")
    }
}
