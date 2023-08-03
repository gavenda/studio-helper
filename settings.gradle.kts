rootProject.name = "studio-helper"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "common",
    "bot:lumi",
    "bot:parrot",
    "bot:vivy",
    "extension:about",
    "extension:administration",
    "extension:announcer",
    "extension:automove",
    "extension:counter",
    "extension:information",
    "extension:listenmoe",
    "extension:moderation",
    "extension:music",
    "extension:utility",
    "library:database",
    "library:metrics"
)

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
        maven {
            name = "Sonatype Snapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
        maven {
            name = "Kotlin Discord"
            url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
            content {
                includeGroup("com.kotlindiscord.kord.extensions")
            }
        }
        maven {
            name = "DRSchlaubi Releases"
            url = uri("https://schlaubi.jfrog.io/artifactory/lavakord")
            content {
                includeGroup("dev.schlaubi.lavakord")
            }
        }
        maven {
            name = "Jitpack"
            url = uri("https://jitpack.io/")
            content {
                includeGroupByRegex("com\\.github.*")
            }
        }
    }
}