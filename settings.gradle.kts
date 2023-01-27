rootProject.name = "studio-helper"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "common",
    "bot:basura",
    "bot:lumi",
    "bot:parrot",
    "bot:vivy",
    "extension:about",
    "extension:administration",
    "extension:anilist",
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
            name = "Dv8tion"
            url = uri("https://m2.dv8tion.net/releases")
        }
        maven {
            name = "Kotlin Discord"
            url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
        }
        maven {
            name = "DRSchlaubi Releases"
            url = uri("https://schlaubi.jfrog.io/artifactory/lavakord")
        }
        maven {
            name = "JCenter"
            url = uri("https://jcenter.bintray.com/")
        }
        maven {
            name = "Jitpack"
            url = uri("https://jitpack.io/")
        }
    }
}