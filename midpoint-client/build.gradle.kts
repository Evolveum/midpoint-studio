
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

    implementation(libs.midpoint.model.api) {
        isTransitive = false
    }

    implementation(libs.midpoint.model.smart.api) {
        isTransitive = false
    }

    implementation(libs.okhttp3)
    implementation(libs.okhttp.logging)

    testImplementation(testLibs.jupiter.api)
    testImplementation(testLibs.mockwebserver)
    testRuntimeOnly(testLibs.jupiter.engine)
    // excluded from midpoint-common above (provided by IDE at runtime), needed on test classpath
    testRuntimeOnly(libs.slf4j.api)
    testRuntimeOnly("xerces:xercesImpl:2.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
