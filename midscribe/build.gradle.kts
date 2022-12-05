fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java-library")
}

dependencies {
    implementation(libs.commons.io)
    implementation(libs.asciidoctorj)
    implementation(libs.asciidoctorj.pdf)
    implementation(libs.asciidoctorj.pdf)
    implementation(libs.asciidoctorj.tabbed.code)
    implementation(libs.velocity)
    implementation(libs.midpoint.schema)

    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
