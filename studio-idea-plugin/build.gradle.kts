import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import java.nio.charset.StandardCharsets
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

// todo doesn't work in multi-module projects, there's ticket for it somewhere
//  providers.gradleProperty(key)
fun properties(key: String): Provider<String> = provider {
    if (project.hasProperty(key)) project.findProperty(key)?.toString() else null
}

fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java")

    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin

    id("antlr") // ANTLR4 plugin
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

var publishChannel = properties("publishChannel").get()
var buildNumber = properties("buildNumber").get()

if (publishChannel == "stable") {
    publishChannel = "default"
}

if (gradle.startParameter.taskNames.contains("publishPlugin")) {
    if (publishChannel != "default"
        && publishChannel != "snapshot"
        && publishChannel != "support"
    ) {
        throw GradleException("Invalid publish channel: $publishChannel")
    }

    val stream = project.file("src/main/java/com/evolveum/midpoint/studio/MidPointConstants.java").inputStream()
    val defaultMidpointVersion = stream.use {
        IOUtils.readLines(
            stream,
            StandardCharsets.UTF_8
        ).stream()
            .filter({ it.contains("DEFAULT_MIDPOINT_VERSION") })
            .map { it.trim() }
            .findFirst()
            .orElse(null)
    }

    println("Default midpoint version: $defaultMidpointVersion")

    if (publishChannel == "default"
        && (defaultMidpointVersion == null || defaultMidpointVersion.contains("SNAPSHOT"))
    ) {

        throw GradleException(
            "Cannot publish to the default channel with '$defaultMidpointVersion' as default midPoint version in constants"
        )
    }
}

var pluginVersionSuffix =
    if (publishChannel != "" && publishChannel != "default")
        "-$publishChannel-$buildNumber"
    else
        ""

var pluginVersion = "$version$pluginVersionSuffix"

println("Plugin version: $pluginVersion")
println("Publish channel: $publishChannel")

repositories {
    maven("https://nexus.evolveum.com/nexus/repository/intellij-dependencies/")
    maven("https://nexus.evolveum.com/nexus/repository/intellij-repository/")
    maven("https://nexus.evolveum.com/nexus/repository/jetbrains-marketplace/")

    // nexus proxy for jetbrainsIdeInstallers()
    ivy {
        url = uri("https://nexus.evolveum.com/nexus/repository/jetbrains-ide-installers/")
        layout("maven")
        patternLayout {
            artifact("[organization]/[module]-[revision](-[classifier]).[ext]")
        }
        metadataSources {
            artifact()
        }
    }

    intellijPlatform {
        localPlatformArtifacts()
    }

    maven("https://nexus.evolveum.com/nexus/repository/intellij-plugin-verifier/")
    maven("https://nexus.evolveum.com/nexus/repository/intellij-jbr/")
}

val platformVersion = properties("platformVersion").get()
val useInstaller = !platformVersion.contains("SNAPSHOT")

dependencies {
    // implementation(libs.annotations)

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"), useInstaller = useInstaller)

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(properties("platformBundledPlugins").map { it.split(',').map(String::trim) })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(properties("platformPlugins").map { it.split(',').map(String::trim) })

        jetbrainsRuntime()
        instrumentationTools()
        pluginVerifier()
        testFramework(TestFrameworkType.Platform)
    }

    antlr("org.antlr:antlr4:4.10.1") {
        exclude("com.ibm.icu")
    }
    implementation("org.antlr:antlr4-runtime:4.10.1")
    implementation("org.antlr:antlr4-intellij-adaptor:0.1")

    implementation(projects.midpointClient)

    implementation(libs.midscribe.core)

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
    implementation(libs.midpoint.security.api) {
        isTransitive = false
    }
    implementation(libs.notifications.api) {
        isTransitive = false
    }
    implementation(libs.midpoint.localization)
//    implementation(libs.midpoint.client)

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

intellijPlatform {
    pluginConfiguration {
        version = pluginVersion

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }

//    signing {
//        certificateChain = environment("CERTIFICATE_CHAIN")
//        privateKey = environment("PRIVATE_KEY")
//        password = environment("PRIVATE_KEY_PASSWORD")
//    }

    publishing {
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = listOf(publishChannel)
        hidden = true
    }

    verifyPlugin {
        ides {
            select {
                types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
                channels = listOf(
                    ProductRelease.Channel.RELEASE,
                    ProductRelease.Channel.EAP,
                    ProductRelease.Channel.BETA,
                    ProductRelease.Channel.RC
                )

                sinceBuild = properties("pluginSinceBuild")
                untilBuild = properties("pluginUntilBuild")
            }
        }
    }
}

//intellijPlatformTesting {
//    testIdeUi {
//        register("testIdeUi") {
//            task {
//                jvmArgumentProviders += CommandLineArgumentProvider {
//                    listOf(
//                        "-Drobot-server.port=8082",
//                        "-Dide.mac.message.dialogs.as.sheets=false",
//                        "-Djb.privacy.policy.text=<!--999.999-->",
//                        "-Djb.consents.confirmation.enabled=false",
//                    )
//                }
//            }
//
//            plugins {
//                robotServerPlugin()
//            }
//        }
//    }
//}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
    // this supports old versions like x.y as well as semantic version x.y.z
    headerParserRegex.set("""(^(0|[1-9]\d*)(\.(0|[1-9]\d*)){1,2})""".toRegex())
}

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    runIde {
        jvmArgs("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
        systemProperty("idea.log.debug.categories", "#com.evolveum.midpoint.studio:all")
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
//    testIdeUi {
//        systemProperty("robot-server.port", "8082")
//        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
//        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
//        systemProperty("jb.consents.confirmation.enabled", "false")
//    }

    publishPlugin {
        dependsOn(patchChangelog)
    }

    generateGrammarSource {
    }

    printProductsReleases {
        channels = listOf(
            ProductRelease.Channel.EAP,
            ProductRelease.Channel.RELEASE,
            ProductRelease.Channel.BETA,
            ProductRelease.Channel.RC
        )
        types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)

        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")
    }
}

tasks.getByName("compileKotlin").dependsOn("generateGrammarSource")

/**
 * This scripts remove all IntelliJ Platform extracted copies from the Gradle Transformer Cache.
 * Needed because of https://github.com/JetBrains/intellij-platform-gradle-plugin/issues/1601
 */
tasks.register("cleanupGradleTransformCache") {
    doLast({
        val userHome = System.getProperty("user.home")
        val caches = File(userHome, ".gradle/caches").toPath()

        val transforms = caches
            .listDirectoryEntries("*")
            .mapNotNull { entry -> entry.resolve("transforms").takeIf { it.exists() } }
            .plus(listOfNotNull(caches.resolve("transforms-4").takeIf { it.exists() }))
        val entries = transforms.flatMap { it.listDirectoryEntries() }

        entries.forEach { entry ->
            val container = entry
                .resolve("transformed")
                .takeIf { it.exists() }
                ?.listDirectoryEntries()
                ?.firstOrNull { it.isDirectory() }
                ?: return@forEach

            val productInfoExists = container.resolve("product-info.json").exists() ||
                    container.resolve("Resources/product-info.json").exists()
            val buildExists = container.resolve("build.txt").exists()
            val hasIdeaDirectory = container.startsWith("idea")

            if (productInfoExists || buildExists || hasIdeaDirectory) {
                println("DELETING: $container")

                FileUtils.deleteDirectory(entry.toFile())
            }
        }
    })
}