import org.gradle.api.Project

val Project.LAVAPLAYER_NATIVE_EXTRAS
    get() = files("$rootDir/lib/lavaplayer-natives-extra-1.3.13.jar")