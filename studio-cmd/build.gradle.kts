fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.springframework.boot") version "2.7.6"
}

group = "com.evolveum.midpoint.studio"
version = "4.4"

dependencies {
    implementation(projects.midpointClient)
    implementation(projects.midscribe)

    implementation(libs.jcommander)
    implementation(libs.commons.io)
    implementation(libs.commons.lang)
    implementation(libs.logback.classic)

    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

springBoot {
    mainClass.set("com.evolveum.midpoint.studio.cmd.StudioCmdMain")
}
