fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

allprojects {
    repositories {
        mavenLocal()
        maven("https://nexus.evolveum.com/nexus/repository/public/")
        maven("https://nexus.evolveum.com/nexus/repository/snapshots/")
    }
}

subprojects {
    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            sourceCompatibility = properties("javaVersion").get()
            targetCompatibility = properties("javaVersion").get()
        }
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }
}
