fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java-library")
}

group = "com.evolveum.midpoint.sdk"
version = "4.9.0"

dependencies {
    api(libs.prism.api)
    api(libs.prism.impl)
    api(libs.axiom)

    compileOnly(libs.slf4j.api)
    implementation(libs.spring.boot.loader)
}

tasks.test {
    useJUnitPlatform()
}
