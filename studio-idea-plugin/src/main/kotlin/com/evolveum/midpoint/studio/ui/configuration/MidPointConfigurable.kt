package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.studio.MidPointConstants
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration
import com.evolveum.midpoint.studio.impl.configuration.MidPointService
import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
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

        val service = MidPointService.getInstance(project)
        service.settings = configuration
    }

    override fun reset() {
        configuration = loadConfiguration()

        super.reset()
    }

    private fun loadConfiguration(): MidPointConfiguration {
        val service = MidPointService.getInstance(project)
        return service.settings.copy()
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return downloadFilePattern?.component
    }

    override fun createPanel(): DialogPanel {
        return panel {
            groupRowsRange(message("MidPointConfigurable.general")) {
                row(message("MidPointConfigurable.midpointVersion")) {
                    comboBox(
                        MidPointConstants.SUPPORTED_VERSIONS,
                        SimpleListCellRenderer.create("") { it }
                    )
                        .bindItem(
                            { configuration.midpointVersion },
                            { configuration.midpointVersion = it })
                        .validationOnApply(::validateNotNull)
                }
                row {
                    checkBox(message("MidPointConfigurable.updateOnUpload"))
                        .bindSelected(
                            { configuration.isUpdateOnUpload },
                            { configuration.isUpdateOnUpload = it }
                        )
                        .comment(message("MidPointConfigurable.updateOnUpload.comment"))
                }
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
        }
    }
}
