plugins {
    application
    alias(libs.plugins.kotlin.jvm)
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
        archiveBaseName.set("bogus-bot-basura")
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}