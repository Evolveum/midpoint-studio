fun properties(key: String) = project.findProperty(key)?.toString()

plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = properties("group")!!
version = "1.0-SNAPSHOT"
description = properties("description")!!

repositories {
    maven("https://nexus.evolveum.com/nexus/repository/public/")
}

dependencies {
    implementation(gradleApi())

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

gradlePlugin {
    plugins {
        create("midpointPlugin") {
            id = properties("pluginId")
            displayName = properties("pluginDisplayName")
            implementationClass = properties("pluginImplementationClass")
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = properties("pluginId")
            artifactId = properties("artifactId")
            version = version.toString()

            from(components["java"])

            pom {
                name.set(properties("pluginDisplayName"))
                description.set(project.description)
                url.set(properties("website"))

                packaging = "jar"

                scm {
                    connection.set(properties("scmUrl"))
                    developerConnection.set(properties("scmUrl"))
                    url.set(properties("vcsUrl"))
                }

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("viliam.repan")
                        name.set("Viliam Repan")
                        email.set("viliam.repan@evolveum.com")
                    }
                }
            }
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
