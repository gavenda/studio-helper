rootProject.name = "studio-helper"

include(
    "common",
    "bot:basura",
    "bot:chupa",
    "bot:lumi",
    "bot:parrot",
    "bot:vivy",
    "extension:about",
    "extension:anilist",
    "extension:aniradio",
    "extension:announcer",
    "extension:automove",
    "extension:counter",
    "extension:moderation",
    "extension:music",
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