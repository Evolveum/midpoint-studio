allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://nexus.evolveum.com/nexus/content/repositories/releases/")
        }
        maven {
            url = uri("https://nexus.evolveum.com/nexus/content/groups/public/")
        }
        maven {
            url = uri("https://nexus.evolveum.com/nexus/content/repositories/snapshots/")
        }
        maven {
            url = uri("https://www.jetbrains.com/intellij-repository/snapshots/")
        }
    }
}

subprojects {
    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }
    }
}
