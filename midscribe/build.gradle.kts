fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java-library")
}

dependencies {
    implementation(libs.slf4j.api)
    implementation(libs.commons.io)
    implementation(libs.asciidoctorj)
    implementation(libs.asciidoctorj.pdf)
    implementation(libs.asciidoctorj.tabbed.code)
    implementation(libs.velocity)

    implementation(libs.midpoint.schema)

    testImplementation(testLibs.jupiter.api)
    testRuntimeOnly(testLibs.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
