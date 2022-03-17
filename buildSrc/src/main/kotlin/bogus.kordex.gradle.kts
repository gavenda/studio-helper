plugins {
    id("bogus.common")
}

repositories {
    mavenCentral()

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.2-SNAPSHOT")
}