fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java-library")
}

group = "com.evolveum.midpoint.sdk"
version = "4.9.0"

dependencies {
    implementation(projects.midpointSdkApi)

    implementation(libs.prism.utils)

    implementation(libs.midpoint.common) {
        isTransitive = false
    }
    implementation(libs.midpoint.schema) {
        isTransitive = false
    }
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
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}