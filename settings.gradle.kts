rootProject.name = "midpoint-studio"

pluginManagement {
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }
}

include("midpoint-client")
include("studio-idea-plugin")

