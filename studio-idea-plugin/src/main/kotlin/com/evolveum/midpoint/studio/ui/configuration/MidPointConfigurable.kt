package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration
import com.evolveum.midpoint.studio.impl.configuration.MidPointService
import com.evolveum.midpoint.studio.util.MavenUtils
import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import javax.swing.JComponent

/**
 * Created by Viliam Repan (lazyman).
 */
open class MidPointConfigurable(val project: Project) :
    BoundSearchableConfigurable(message("MidPointConfigurable.title"), "") {

    private var downloadFilePattern: Cell<JBTextField>? = null

    private var configuration: MidPointConfiguration

    init {
        configuration = loadConfiguration()
    }

    override fun apply() {
        super.apply()

        val service = MidPointService.get(project)
        service.settings = configuration
    }

    override fun reset() {
        configuration = loadConfiguration()

        super.reset()
    }

    private fun loadConfiguration(): MidPointConfiguration {
        val service = MidPointService.get(project)
        return service.settings.copy()
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return downloadFilePattern?.component
    }

    override fun createPanel(): DialogPanel {
        return panel {
            groupRowsRange(message("MidPointConfigurable.general")) {
//                row(message("MidPointConfigurable.midpointVersion")) {
//                    comboBox(
//                        MidPointConstants.SUPPORTED_VERSIONS,
//                        SimpleListCellRenderer.create("") { it }
//                    )
//                        .bindItem(
//                            { configuration.midpointVersion },
//                            { configuration.midpointVersion = it })
//                        .validationOnApply(::validateNotNull)
//                        .validationOnInput(::validateMidpointVersion)
//                }
                row {
                    checkBox(message("MidPointConfigurable.updateOnUpload"))
                        .bindSelected(
                            { configuration.isUpdateOnUpload },
                            { configuration.isUpdateOnUpload = it }
                        )
                        .comment(message("MidPointConfigurable.updateOnUpload.comment"))
                }
                row {
                    checkBox(message("MidPointConfigurable.ignoreMissingKeys"))
                        .bindSelected(
                            { configuration.isIgnoreMissingKeys },
                            { configuration.isIgnoreMissingKeys = it }
                        )
                        .comment(message("MidPointConfigurable.ignoreMissingKeys.comment"))
                }
                row(message("MidPointConfigurable.cacheTTL")) {
                    intTextField(IntRange(0, 60 * 60 * 24), 5)
                        .bindIntText(
                            { configuration.cacheTTL },
                            { configuration.cacheTTL = it }
                        )
                }.comment(message("MidPointConfigurable.cacheTTL.comment"))
            }
            groupRowsRange(message("MidPointConfigurable.restClient.title")) {
                row(message("MidPointConfigurable.restClient.downloadFilePattern")) {
                    downloadFilePattern = textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(
                            { configuration.dowloadFilePattern },
                            { configuration.dowloadFilePattern = it })
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                        .focused()
                }
                row(message("MidPointConfigurable.restClient.generatedFilePattern")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(
                            { configuration.generatedFilePattern },
                            { configuration.generatedFilePattern = it }
                        )
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                }
                row(message("MidPointConfigurable.restClient.timeout")) {
                    intTextField(IntRange(1, 3600), 1)
                        .bindIntText(
                            { configuration.restResponseTimeout },
                            { configuration.restResponseTimeout = it }
                        )
                }.comment(message("MidPointConfigurable.restClient.timeout.comment"))
                row {
                    checkBox(message("MidPointConfigurable.restClient.logCommunication"))
                        .bindSelected(
                            { configuration.isPrintRestCommunicationToConsole },
                            { configuration.isPrintRestCommunicationToConsole = it }
                        )
                }
            }
            groupRowsRange(message("MidPointConfigurable.download.title")) {
                row(message("MidPointConfigurable.download.include")) {
                    expandableTextField()
                        .validationOnInput(::validateTypes)
                        .validationOnApply(::validateTypes)
                        .bindText(
                            { convertObjectTypesListToString(configuration.downloadTypesInclude) },
                            { configuration.downloadTypesInclude = convertStringToObjectTypesList(it) })
                        .align(AlignX.FILL)
                }
                row(message("MidPointConfigurable.download.exclude")) {
                    expandableTextField()
                        .validationOnInput(::validateTypes)
                        .validationOnApply(::validateTypes)
                        .bindText(
                            { convertObjectTypesListToString(configuration.downloadTypesExclude) },
                            { configuration.downloadTypesExclude = convertStringToObjectTypesList(it) })
                        .align(AlignX.FILL)
                }
                row(message("MidPointConfigurable.download.limit")) {
                    intTextField(IntRange(1, 500), 1)
                        .bindIntText(
                            { configuration.typesToDownloadLimit },
                            { configuration.typesToDownloadLimit = it })
                }
            }
            groupRowsRange(message("MidPointConfigurable.serverLog.title")) {
                row(message("MidPointConfigurable.serverLog.pollInterval")) {
                    intTextField(IntRange(1, 3600), 1)
                        .bindIntText(
                            { configuration.logPollInterval },
                            { configuration.logPollInterval = it })
                }
                row(message("MidPointConfigurable.serverLog.initialTailSize")) {
                    intTextField(IntRange(1, 100 * 1024), 1)
                        .bindIntText(
                            { configuration.logInitialTailSize },
                            { configuration.logInitialTailSize = it })
                }
                row(message("MidPointConfigurable.serverLog.maxChunkSize")) {
                    intTextField(IntRange(1, 100 * 1024), 1)
                        .bindIntText(
                            { configuration.logMaxChunkSize },
                            { configuration.logMaxChunkSize = it })
                }
                row(message("MidPointConfigurable.serverLog.bufferEntries")) {
                    intTextField(IntRange(100, 1000000), 1)
                        .bindIntText(
                            { configuration.logBufferEntries },
                            { configuration.logBufferEntries = it })
                }
                row(message("MidPointConfigurable.serverLog.captureMaxFileSize")) {
                    intTextField(IntRange(1, 10 * 1024), 1)
                        .bindIntText(
                            { configuration.logCaptureMaxFileSize },
                            { configuration.logCaptureMaxFileSize = it })
                }
                row(message("MidPointConfigurable.serverLog.captureMaxFiles")) {
                    intTextField(IntRange(1, 100), 1)
                        .bindIntText(
                            { configuration.logCaptureMaxFiles },
                            { configuration.logCaptureMaxFiles = it })
                }
                row(message("MidPointConfigurable.serverLog.entryStartPattern")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(
                            { configuration.logEntryStartPattern ?: "" },
                            { configuration.logEntryStartPattern = it })
                        .validationOnInput(::validateRegex)
                        .validationOnApply(::validateRegex)
                }.comment(message("MidPointConfigurable.serverLog.entryStartPattern.comment"))
            }
        }
    }

    private fun validateMidpointVersion(builder: ValidationInfoBuilder, combo: ComboBox<String>): ValidationInfo? {
        val version = combo.item

        val dependencies = MavenUtils.getMidpointDependencies(project)
        if (dependencies.isEmpty()) {
            return builder.warning("No midpoint dependencies found in maven project.")
        }

        val versions = dependencies.stream()
            .map { it.version }
            .distinct()
            .sorted()
            .toList()

        if (versions.size > 1) {
            return builder.warning(
                "MidPoint dependencies fount in maven project have different versions (" + versions.joinToString(
                    ", "
                ) + ")."
            )
        }

        val mvnVersion = versions[0]

        if (!mvnVersion.startsWith(version)) {
            return builder.warning("MidPoint dependencies found in maven project have different version (" + mvnVersion + ") than selected.")
        }

        return null
    }
}
