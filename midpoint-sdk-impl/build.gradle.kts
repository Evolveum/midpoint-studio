import org.springframework.boot.gradle.tasks.bundling.BootJar

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java-library")
    id("org.springframework.boot") version "3.1.5"
}

group = "com.evolveum.midpoint.sdk"
version = "4.9.0"

dependencies {
    api(projects.midpointSdkApi)

    implementation(libs.midpoint.schema) {
        exclude("com.evolveum.prism", "prism-api")
        exclude("com.evolveum.prism", "prism-impl")
    }
    implementation(libs.midpoint.localization)

    implementation(libs.midpoint.model.common) {
        isTransitive = false
    }
    implementation(libs.midpoint.model.api) {
        isTransitive = false
    }
    implementation(libs.midpoint.model.impl) {
        isTransitive = false
    }
    implementation(libs.midpoint.notifications.api) {
        isTransitive = false
    }
    implementation(libs.midpoint.security.api) {
        isTransitive = false
    }

    implementation(libs.commons.lang)

    testImplementation(testLibs.jupiter.api)
    testRuntimeOnly(testLibs.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.getByName<BootJar>("bootJar") {
    archiveClassifier.set("all")
    mainClass.set("NonExistingClass")
}

tasks.getByName<Jar>("jar") {
    archiveClassifier.set("")
}