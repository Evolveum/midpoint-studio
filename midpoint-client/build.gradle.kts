
fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java-library")
}

dependencies {
    api(libs.midpoint.common) {
        exclude("org.springframework")
        exclude("net.sf.jasperreports")
        exclude("org.apache.cxf")
        exclude("org.slf4j")
        exclude("ch.qos.logback")
        exclude("xerces")
    }

    implementation(libs.midpoint.model.smart.api) {
        isTransitive = false
    }

    implementation(libs.okhttp3)
    implementation(libs.okhttp.logging)

    testImplementation(testLibs.jupiter.api)
    testRuntimeOnly(testLibs.jupiter.engine)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
