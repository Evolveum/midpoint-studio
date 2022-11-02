allprojects {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://nexus.evolveum.com/nexus/content/groups/public/")
        }
        maven {
            url = uri("https://nexus.evolveum.com/nexus/content/repositories/snapshots/")
        }
        maven {
            url = uri("https://nexus.evolveum.com/nexus/repository/intellij-repository/")
        }
        maven {
            url = uri("https://nexus.evolveum.com/nexus/repository/intellij-dependencies/")
        }
        maven {
            url = uri("https://nexus.evolveum.com/nexus/repository/intellij-plugin-verifier/")
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
