rootProject.name = "midpoint-studio"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("asciidoctorj-tabbed-code", "0.3")
            version("commons-io", "2.9.0")
            version("commons-lang", "3.10")
            version("jaxb-runtime", "2.3.2")
            version("jcommander", "1.81")
            version("jupiter", "5.6.0")
            version("logback", "1.2.3")
            version("midpoint", "4.4")
            version("midscribe", "4.3-SNAPSHOT")
            version("okhttp", "4.9.0")
            version("openkeepass", "0.8.1")
            version("slf4j", "1.7.26")
            version("spring", "5.2.8.RELEASE")
            version("stax", "1.2.0")
            version("testng", "6.14.3")
            version("xchart", "3.5.4")
            version("xml-apis", "1.4.01")
            version("remote-robot", "0.11.7")
            version("xmlunit", "2.8.3")

            alias("asciidoctorj-tabbed-code").to("com.bmuschko", "asciidoctorj-tabbed-code-extension")
                .versionRef("asciidoctorj-tabbed-code")
            alias("common").to("com.evolveum.midpoint.infra", "common").versionRef("midpoint")
            alias("commons-io").to("commons-io", "commons-io").versionRef("commons-io")
            alias("commons-lang").to("org.apache.commons", "commons-lang3").versionRef("commons-lang")
            alias("jaxb-runtime").to("org.glassfish.jaxb", "jaxb-runtime").versionRef("jaxb-runtime")
            alias("jcommander").to("com.beust", "jcommander").versionRef("jcommander")
            alias("jupiter-api").to("org.junit.jupiter", "junit-jupiter-api").versionRef("jupiter")
            alias("jupiter-engine").to("org.junit.jupiter", "junit-jupiter-engine").withoutVersion()
            alias("logback-classic").to("ch.qos.logback", "logback-classic").versionRef("logback")
            alias("midpoint-localization").to("com.evolveum.midpoint", "midpoint-localization").versionRef("midpoint")
            alias("midscribe-core").to("com.evolveum.midpoint", "midscribe-core").versionRef("midscribe")
            alias("model-api").to("com.evolveum.midpoint.model", "model-api").versionRef("midpoint")
            alias("model-common").to("com.evolveum.midpoint.model", "model-common").versionRef("midpoint")
            alias("model-impl").to("com.evolveum.midpoint.model", "model-impl").versionRef("midpoint")
            alias("notifications-api").to("com.evolveum.midpoint.model", "notifications-api").versionRef("midpoint")
            alias("okhttp-logging").to("com.squareup.okhttp3", "logging-interceptor").versionRef("okhttp")
            alias("okhttp3").to("com.squareup.okhttp3", "okhttp").versionRef("okhttp")
            alias("openkeepass").to("de.slackspace", "openkeepass").versionRef("openkeepass")
            alias("security-api").to("com.evolveum.midpoint.repo", "security-api").versionRef("midpoint")
            alias("slf4j-log4j12").to("org.slf4j", "slf4j-log4j12").versionRef("slf4j")
            alias("spring-core").to("org.springframework", "spring-core").versionRef("spring")
            alias("stax").to("stax", "stax").versionRef("stax")
            alias("xchart").to("org.knowm.xchart", "xchart").versionRef("xchart")
            alias("xml-apis").to("xml-apis", "xml-apis").versionRef("xml-apis")
            alias("remote-robot").to("com.intellij.remoterobot", "remote-robot").versionRef("remote-robot")
            alias("remote-fixtures").to("com.intellij.remoterobot", "remote-fixtures").versionRef("remote-robot")
            alias("xmlunit-core").to("org.xmlunit", "xmlunit-core").versionRef("xmlunit")
        }
    }
}

include("midpoint-client")
include("studio-idea-plugin")

