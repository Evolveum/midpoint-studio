rootProject.name = "midpoint-studio"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://nexus.evolveum.com/nexus/repository/gradle-plugins/")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("asciidoctorj-tabbed-code", "0.3")
            version("commons-io", "2.11.0")
            version("commons-lang", "3.10")
            version("jaxb-runtime", "2.3.2")
            version("jcommander", "1.81")
            version("logback", "1.2.3")
            version("midpoint", "4.8-M3")
            version("okhttp", "4.10.0")
            version("openkeepass", "0.8.1")
            version("spring", "5.3.8")
            version("stax", "1.2.0")
            version("testng", "6.14.3")
            version("slf4j", "1.7.32")
            version("asciidoctorj", "2.5.3")
            version("asciidoctorj-pdf", "1.6.2")
            version("asciidoctorj-tabbed-code", "0.3")
            version("velocity", "2.3")
            version("jruby", "9.2.19.0")
            version("antlr", "4.10.1")

            library("asciidoctorj", "org.asciidoctor", "asciidoctorj").versionRef("asciidoctorj")
            library("asciidoctorj-pdf", "org.asciidoctor", "asciidoctorj-pdf").versionRef("asciidoctorj-pdf")
            library(
                "asciidoctorj-tabbed-code",
                "com.bmuschko",
                "asciidoctorj-tabbed-code-extension"
            ).versionRef("asciidoctorj-tabbed-code")
            library("commons-io", "commons-io", "commons-io").versionRef("commons-io")
            library("commons-lang", "org.apache.commons", "commons-lang3").versionRef("commons-lang")
            library("jaxb-runtime", "org.glassfish.jaxb", "jaxb-runtime").versionRef("jaxb-runtime")
            library("jcommander", "com.beust", "jcommander").versionRef("jcommander")
            library("logback-classic", "ch.qos.logback", "logback-classic").versionRef("logback")
            library("midpoint-common", "com.evolveum.midpoint.infra", "common").versionRef("midpoint")
            library("midpoint-localization", "com.evolveum.midpoint", "midpoint-localization").versionRef("midpoint")
            library("midpoint-model-api", "com.evolveum.midpoint.model", "model-api").versionRef("midpoint")
            library("midpoint-model-common", "com.evolveum.midpoint.model", "model-common").versionRef("midpoint")
            library("midpoint-model-impl", "com.evolveum.midpoint.model", "model-impl").versionRef("midpoint")
            library("midpoint-schema", "com.evolveum.midpoint.infra", "schema").versionRef("midpoint")
            library("notifications-api", "com.evolveum.midpoint.model", "notifications-api").versionRef("midpoint")
            library("okhttp-logging", "com.squareup.okhttp3", "logging-interceptor").versionRef("okhttp")
            library("okhttp3", "com.squareup.okhttp3", "okhttp").versionRef("okhttp")
            library("openkeepass", "de.slackspace", "openkeepass").versionRef("openkeepass")
            library("security-api", "com.evolveum.midpoint.repo", "security-api").versionRef("midpoint")
            library("spring-core", "org.springframework", "spring-core").versionRef("spring")
            library("stax", "stax", "stax").versionRef("stax")
            library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("velocity", "org.apache.velocity", "velocity-engine-core").versionRef("velocity")
            library("antlr", "org.antlr", "antlr4").versionRef("antlr")
        }
        create("testLibs") {
            version("jupiter", "5.8.1")
            version("remote-robot", "0.11.7")
            version("xmlunit", "2.8.3")
            version("xalan", "2.7.2")

            library("jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("jupiter")
            library("jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()
            library("remote-robot", "com.intellij.remoterobot", "remote-robot").versionRef("remote-robot")
            library("remote-fixtures", "com.intellij.remoterobot", "remote-fixtures").versionRef("remote-robot")
            library("xmlunit-core", "org.xmlunit", "xmlunit-core").versionRef("xmlunit")
            library("xalan", "xalan", "xalan").versionRef("xalan")
        }
    }
}

include("midpoint-client")
include("midscribe")
include("studio-cmd")
include("studio-idea-plugin")
include("studio-gradle-plugin")
