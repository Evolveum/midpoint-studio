plugins {
    id("java")
}

group = "com.evolveum.midpoint.sdk"
version = "4.9.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.midpoint.common) {
        isTransitive = false
        because("Needed because of LocalizationService interface")      // todo move LocalizationService interface somewhere maybe?
    }
    runtimeOnly(libs.midpoint.localization) {
        because("For LocalizationServiceImpl")
    }

    implementation(libs.slf4j.api)
}

tasks.test {
    useJUnitPlatform()
}