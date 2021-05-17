
fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
}

group = "com.evolveum.midpoint.studio"
version = "4.4"

dependencies {
    implementation("com.evolveum.midpoint.infra:common:" + properties("midpointVersion")) {
        exclude("org.springframework")
        exclude("net.sf.jasperreports")
        exclude("org.apache.cxf")
        exclude("org.slf4j")
        exclude("ch.qos.logback")
    }

    implementation("com.squareup.okhttp3:okhttp:" + properties("okHttpVersion"))
    implementation("com.squareup.okhttp3:logging-interceptor:" + properties("okHttpVersion"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
