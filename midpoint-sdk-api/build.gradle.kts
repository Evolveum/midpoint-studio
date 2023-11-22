plugins {
    id("java-library")
}

group = "com.evolveum.midpoint.sdk"
version = "4.9.0"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.midpoint.schema) {
        because("For prism and schema related features")
    }
    implementation(libs.midpoint.common) {
        isTransitive = false
        because("Needed only because of LocalizationService interface")      // todo move LocalizationService interface somewhere maybe?
    }
    runtimeOnly(libs.midpoint.localization) {
        because("For LocalizationServiceImpl")
    }

    implementation(libs.slf4j.api)
    implementation(libs.spring.boot.loader)
}

tasks.test {
    useJUnitPlatform()
}