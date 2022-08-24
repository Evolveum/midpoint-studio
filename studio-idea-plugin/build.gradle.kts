import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.intellij.tasks.RunPluginVerifierTask

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.8.0"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "1.3.1"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    // git plugin - read more: https://github.com/palantir/gradle-git-version
    id("com.palantir.git-version") version "0.12.2"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra

// Configure project's dependencies
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")

    implementation(projects.midpointClient)

    implementation(libs.model.common) {
        isTransitive = false
    }
    implementation(libs.model.api) {
        isTransitive = false
    }
    implementation(libs.model.impl) {
        isTransitive = false
    }
    implementation(libs.common) {
        exclude("org.springframework")
        exclude("net.sf.jasperreports")
        exclude("org.apache.cxf")
        exclude("org.slf4j")
        exclude("ch.qos.logback")
    }
    implementation(libs.security.api) {
        isTransitive = false
    }
    implementation(libs.notifications.api) {
        isTransitive = false
    }
    implementation(libs.midpoint.localization)

    implementation(libs.midscribe.core) {
        exclude("org.springframework")
        exclude("net.sf.jasperreports")
        exclude("org.apache.cxf", "cxf-rt-wsdl")

        exclude("org.slf4j")
        exclude("ch.qos.logback")
    }
    implementation(libs.asciidoctorj.tabbed.code)
    implementation("org.apache.velocity", "velocity-engine-core")

    implementation(libs.openkeepass)
    implementation(libs.commons.lang)
    // compile(libs.xchart)
    implementation(libs.okhttp3)
    implementation(libs.okhttp.logging)

    implementation(libs.stax)
    implementation(libs.xml.apis)

    runtimeOnly(libs.jaxb.runtime) // needed because of NamespacePrefixMapper class
    runtimeOnly(libs.spring.core) {
        // spring-core needed because of DebugDumpable impl uses spring ReflectionUtils class
        isTransitive = false
    }

    testImplementation(libs.jupiter.api)
    testImplementation(libs.remote.robot)
    testImplementation(libs.remote.fixtures)
    testImplementation(libs.xmlunit.core)

    testRuntimeOnly(libs.jupiter.engine)
}

var channel = properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()

var gitLocalBranch = properties("gitLocalBranch")
var publishChannel = properties("publishChannel")
var buildNumber = properties("buildNumber")

if (publishChannel.isBlank() || publishChannel == "null") {
    if (gitLocalBranch.isEmpty() || gitLocalBranch == "null") {
        gitLocalBranch = versionDetails()?.branchName
    }

    publishChannel = if (gitLocalBranch == "stable") "default" else gitLocalBranch
}

var channelSuffix = ""
if (publishChannel.isNotBlank() && publishChannel.toLowerCase() != "default") {
    channelSuffix = "-$publishChannel-$buildNumber"
}

var pluginVersion = "$version$channelSuffix"
channel = publishChannel

// end of version/channel override

println("Plugin version: $pluginVersion")
println("Publish channel: $channel")

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    downloadSources.set(properties("platformDownloadSources").toBoolean())
    updateSinceUntilBuild.set(true)

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))

    intellijRepository.set("https://nexus.evolveum.com/nexus/repository/intellij-repository/")
    jreRepository.set("https://nexus.evolveum.com/nexus/repository/intellij-jbr/")
    pluginsRepositories {
        maven("https://nexus.evolveum.com/nexus/repository/jetbrains-plugins/")
        getRepositories()
    }
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
    // this supports old versions like x.y as well as semantic version x.y.z
    headerParserRegex.set("""(^(0|[1-9]\d*)(\.(0|[1-9]\d*)){1,2})""".toRegex())
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
        version.set(pluginVersion)
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            File(projectDir, "README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        var changelogContent = ""

        val hasChangelog = changelog.has(properties("pluginVersion"))
        if (hasChangelog) {
            changelogContent = changelog.get(properties("pluginVersion")).toHTML()
        } else {
            changelogContent = changelog.getUnreleased().toHTML()
        }
        changeNotes.set(changelogContent)
    }

    runPluginVerifier {
        ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
        failureLevel.set(listOf(
            RunPluginVerifierTask.FailureLevel.COMPATIBILITY_PROBLEMS,
            RunPluginVerifierTask.FailureLevel.MISSING_DEPENDENCIES,
            RunPluginVerifierTask.FailureLevel.INVALID_PLUGIN))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("studio_intellijPublishToken"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(channel))
    }

    test {
        useJUnitPlatform()
    }

    runIde {
        jvmArgs("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
    }

    runIdeForUiTests {
        systemProperty("robot-server.port", "8082") // default port 8580
    }

    downloadRobotServerPlugin {
        version.set("0.11.7")
    }
}
