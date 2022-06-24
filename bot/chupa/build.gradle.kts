plugins {
    id("bogus.kotlin-conventions")
    id("bogus.bot-application")
}

version = "1.0"

application {
    mainClass.set("bogus.bot.chupa.AppKt")
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kordex)
    implementation(libs.bundles.log4j2)

    implementation(project(":common"))
    implementation(project(":extension:about"))
    implementation(project(":extension:counter"))
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-chupa")
    }
}
