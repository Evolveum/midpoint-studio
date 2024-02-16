import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.tasks.RunPluginVerifierTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    // ANTLR4 plugin
    id("antlr")
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.17.1"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "2.2.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

var publishChannel = properties("publishChannel")
var buildNumber = properties("buildNumber")

if (gradle.startParameter.taskNames.contains("publishPlugin")
    && publishChannel != "default"
    && publishChannel != "snapshot"
    && publishChannel != "support") {

    throw GradleException("Invalid publish channel: $publishChannel")
}

var pluginVersionSuffix =
    if (publishChannel != "" && publishChannel != "default")
        "-$publishChannel-$buildNumber"
    else
        ""

var pluginVersion = "$version$pluginVersionSuffix"

println("Plugin version: $pluginVersion")
println("Publish channel: $publishChannel")

var customSandboxDir = System.getProperty("customSandboxDir")
if (customSandboxDir == null || customSandboxDir.isBlank()) {
    customSandboxDir = "${project.buildDir}/idea-sandbox"
}

dependencies {
    antlr("org.antlr:antlr4:4.10.1") {
        exclude("com.ibm.icu")
    }
    implementation("org.antlr:antlr4-runtime:4.10.1")
    implementation("org.antlr:antlr4-intellij-adaptor:0.1")

    implementation(projects.midpointClient)
    implementation(projects.midscribe) {
        exclude("org.springframework")
        exclude("net.sf.jasperreports")
        exclude("org.apache.cxf")
        exclude("org.slf4j")
        exclude("ch.qos.logback")
        exclude("xerces")
    }

    implementation(libs.midpoint.model.common) {
        isTransitive = false
    }
    implementation(libs.midpoint.model.api) {
        isTransitive = false
    }
    implementation(libs.midpoint.model.impl) {
        isTransitive = false
    }
    implementation(libs.midpoint.common) {
        exclude("org.springframework")
        exclude("net.sf.jasperreports")
        exclude("org.apache.cxf")
        exclude("org.slf4j")
        exclude("ch.qos.logback")
        exclude("xerces")
    }
    implementation(libs.security.api) {
        isTransitive = false
    }
    implementation(libs.notifications.api) {
        isTransitive = false
    }
    implementation(libs.midpoint.localization)

    implementation(libs.asciidoctorj.tabbed.code)
    implementation(libs.velocity) {
        exclude("org.slf4j")
    }

    implementation(libs.openkeepass) {
        exclude("stax", "stax-api")
    }
    implementation(libs.commons.lang)
    implementation(libs.okhttp3)
    implementation(libs.okhttp.logging)

    runtimeOnly(libs.jaxb.runtime) // needed because of NamespacePrefixMapper class
    runtimeOnly(libs.spring.core) {
        isTransitive = false
        because("spring-core needed because of DebugDumpable impl uses spring ReflectionUtils class")
    }

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") {
        because("Only needed to run tests in a version of IntelliJ IDEA that bundles older versions")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-engine")

    testImplementation(testLibs.remote.robot)
    testImplementation(testLibs.remote.fixtures)

    testImplementation(testLibs.xmlunit.core)

    testImplementation(testLibs.xalan)
}

kotlin {
    jvmToolchain(17)
}

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

    sandboxDir.set(customSandboxDir)
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
    // this supports old versions like x.y as well as semantic version x.y.z
    headerParserRegex.set("""(^(0|[1-9]\d*)(\.(0|[1-9]\d*)){1,2})""".toRegex())
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = properties("javaVersion")
        targetCompatibility = properties("javaVersion")
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = properties("javaVersion")
    }
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            file("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").let { markdownToHTML(it) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            with(changelog) {
                renderItem(
                    getOrNull(properties("pluginVersion")) ?: getUnreleased(),
                    Changelog.OutputType.HTML,
                )
            }
        })
    }

    runPluginVerifier {
        ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
        failureLevel.set(
            listOf(
                RunPluginVerifierTask.FailureLevel.COMPATIBILITY_PROBLEMS,
                RunPluginVerifierTask.FailureLevel.MISSING_DEPENDENCIES,
                RunPluginVerifierTask.FailureLevel.INVALID_PLUGIN
            )
        )
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(publishChannel))
    }

    test {
        useJUnitPlatform()
    }

    runIde {
        jvmArgs("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
        systemProperties(
            "idea.log.debug.categories" to "#com.evolveum.midpoint.studio:all",
        )
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    downloadRobotServerPlugin {
        version.set("0.11.7")
    }

    generateGrammarSource {
    }
}

tasks.getByName("compileKotlin").dependsOn("generateGrammarSource")
