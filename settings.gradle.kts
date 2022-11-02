rootProject.name = "midpoint-studio"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        maven("https://nexus.evolveum.com/nexus/repository/gradle-plugins/")
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
            version("midpoint", "4.6")
            version("midscribe", "4.4-SNAPSHOT")
            version("okhttp", "4.10.0")
            version("openkeepass", "0.8.1")
            version("spring", "5.3.8")
            version("stax", "1.2.0")
            version("testng", "6.14.3")
            version("remote-robot", "0.11.7")
            version("xmlunit", "2.8.3")
            version("xalan", "2.7.2")

            library("asciidoctorj-tabbed-code", "com.bmuschko", "asciidoctorj-tabbed-code-extension").versionRef("asciidoctorj-tabbed-code")
            library("common", "com.evolveum.midpoint.infra", "common").versionRef("midpoint")
            library("commons-io", "commons-io", "commons-io").versionRef("commons-io")
            library("commons-lang", "org.apache.commons", "commons-lang3").versionRef("commons-lang")
            library("jaxb-runtime", "org.glassfish.jaxb", "jaxb-runtime").versionRef("jaxb-runtime")
            library("jcommander", "com.beust", "jcommander").versionRef("jcommander")
            library("jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("jupiter")
            library("jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()
            library("logback-classic", "ch.qos.logback", "logback-classic").versionRef("logback")
            library("midpoint-localization", "com.evolveum.midpoint", "midpoint-localization").versionRef("midpoint")
            library("midscribe-core", "com.evolveum.midpoint", "midscribe-core").versionRef("midscribe")
            library("model-api", "com.evolveum.midpoint.model", "model-api").versionRef("midpoint")
            library("model-common", "com.evolveum.midpoint.model", "model-common").versionRef("midpoint")
            library("model-impl", "com.evolveum.midpoint.model", "model-impl").versionRef("midpoint")
            library("notifications-api", "com.evolveum.midpoint.model", "notifications-api").versionRef("midpoint")
            library("okhttp-logging", "com.squareup.okhttp3", "logging-interceptor").versionRef("okhttp")
            library("okhttp3", "com.squareup.okhttp3", "okhttp").versionRef("okhttp")
            library("openkeepass", "de.slackspace", "openkeepass").versionRef("openkeepass")
            library("security-api", "com.evolveum.midpoint.repo", "security-api").versionRef("midpoint")
            library("spring-core", "org.springframework", "spring-core").versionRef("spring")
            library("stax", "stax", "stax").versionRef("stax")
            library("remote-robot", "com.intellij.remoterobot", "remote-robot").versionRef("remote-robot")
            library("remote-fixtures", "com.intellij.remoterobot", "remote-fixtures").versionRef("remote-robot")
            library("xmlunit-core", "org.xmlunit", "xmlunit-core").versionRef("xmlunit")
            library("xalan", "xalan", "xalan").versionRef("xalan")
        }
    }
}

include("midpoint-client")
include("studio-idea-plugin")

