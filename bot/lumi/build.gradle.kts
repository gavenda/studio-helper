plugins {
    application
    alias(libs.plugins.kotlin.jvm)
}

version = "1.0"

application {
    mainClass.set("bogus.bot.lumi.AppKt")
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kordex)

    implementation(project(":common"))
    implementation(project(":extension:about"))
    implementation(project(":extension:moderation"))
}

tasks {
    val distDir = file("$rootDir/dist")
    val installDistDir = file("$rootDir/dist/${project.name}")

    named<Sync>(name = "installDist") {
        destinationDir = installDistDir
    }
    named<Zip>(name = "distZip") {
        destinationDirectory.set(distDir)
    }
    named<Tar>(name = "distTar") {
        destinationDirectory.set(distDir)
    }
    withType<org.gradle.jvm.tasks.Jar> {
        archiveBaseName.set("bogus-bot-lumi")
    }
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(11))
    }
}