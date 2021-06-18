import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.closure
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.4.32"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.7.3"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "1.1.2"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.17.0"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.16.0")

    implementation(projects.midpointClient)

    compile(libs.model.common) {
        isTransitive = false
    }
    compile(libs.model.api) {
        isTransitive = false
    }
    compile(libs.model.impl) {
        isTransitive = false
    }
    compile(libs.common) {
        exclude("org.springframework")
        exclude("net.sf.jasperreports")
        exclude("org.apache.cxf")
        exclude("org.slf4j")
        exclude("ch.qos.logback")
    }
    compile(libs.security.api) {
        isTransitive = false
    }
    compile(libs.notifications.api) {
        isTransitive = false
    }
    compile(libs.midpoint.localization)

    compile(libs.midscribe.core) {
        exclude("org.springframework")
        exclude("net.sf.jasperreports")
        exclude("org.apache.cxf", "cxf-rt-wsdl")

        exclude("org.slf4j")
        exclude("ch.qos.logback")
    }
    implementation(libs.asciidoctorj.tabbed.code)

    compile(libs.openkeepass)
    compile(libs.commons.lang)
    // compile(libs.xchart)
    compile(libs.slf4j.log4j12)
    compile(libs.okhttp3)
    compile(libs.okhttp.logging)

    compile(libs.stax)
    compile(libs.xml.apis)

    runtime(libs.jaxb.runtime) // needed because of NamespacePrefixMapper class
    runtime(libs.spring.core) {
        // spring-core needed because of DebugDumpable impl uses spring ReflectionUtils class
        isTransitive = false
    }
}

var channel = properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()

// until we move to semantic versioning and use only stable/nightly (without milestone) channels
// we'll modify "recommended" versions and channel

var gitLocalBranch = properties("gitLocalBranch")
var publishChannel = properties("publishChannel")
var buildNumber = properties("buildNumber")

if (gitLocalBranch == "nightly" || gitLocalBranch == "milestone") {
    publishChannel = gitLocalBranch
} else if (gitLocalBranch == "stable") {
    publishChannel = "default"
}

var channelSuffix = ""
if (!publishChannel.isBlank()) {
    var channel = publishChannel.toLowerCase()
    if (channel == "nightly") {
        channelSuffix = "-$buildNumber-$channel"
    } else if (channel == "milestone") {
        channelSuffix = "-$buildNumber"
    }
}

version = "$version$channelSuffix"
channel = publishChannel

// end of version/channel override

println("Plugin version: $version")
println("Publish channel: $channel")

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")
    downloadSources = properties("platformDownloadSources").toBoolean()
    updateSinceUntilBuild = true

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    setPlugins(*properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty).toTypedArray())
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version = properties("pluginVersion")
    groups = emptyList()
    headerParserRegex =
        "\\d+\\.\\d+"       // this can be remove when we'll start using semantic versioning (e.g. 4.4.0)
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true

    reports {
        html.enabled = false
        xml.enabled = false
        txt.enabled = false
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = properties("javaVersion")
        targetCompatibility = properties("javaVersion")
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = properties("javaVersion")
    }

    withType<Detekt> {
        jvmTarget = properties("javaVersion")
    }

    patchPluginXml {
        version(version)
        sinceBuild(properties("pluginSinceBuild"))
        untilBuild(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription(
            closure {
                File(projectDir, "README.md").readText().lines().run {
                    val start = "<!-- Plugin description -->"
                    val end = "<!-- Plugin description end -->"

                    if (!containsAll(listOf(start, end))) {
                        throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end))
                }.joinToString("\n").run { markdownToHTML(this) }
            }
        )

        // Get the latest available change notes from the changelog file
        changeNotes(
            closure {
                changelog.getLatest().toHTML()
            }
        )
    }

    runPluginVerifier {
        ideVersions(properties("pluginVerifierIdeVersions"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token(System.getenv("studio_intellijPublishToken"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels(channel)
    }
}
