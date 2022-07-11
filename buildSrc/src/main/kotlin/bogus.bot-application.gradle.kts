import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType

plugins {
    application
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
    named<Delete>(name = "clean") {
        delete.add("$rootDir/dist")
    }
}
