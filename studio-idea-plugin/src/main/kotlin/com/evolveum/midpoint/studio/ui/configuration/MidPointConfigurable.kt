package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.studio.MidPointConstants
import com.evolveum.midpoint.studio.impl.MidPointService
import com.evolveum.midpoint.studio.impl.MidPointSettings
import com.evolveum.midpoint.studio.util.StudioBundle.message
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

    private var configuration: MidPointSettings = MidPointSettings.createDefaultSettings()

    override fun apply() {
        super.apply()

        val service = MidPointService.getInstance(project)
        service.settings = configuration!!
    }

    override fun reset() {
        super.reset()

        val service = MidPointService.getInstance(project)
        configuration = service.settings.copy()
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
                        .bindItem(configuration::getMidpointVersion, configuration::setMidpointVersion)
                }
            }
            groupRowsRange(message("MidPointConfigurable.restClient.title")) {
                row(message("MidPointConfigurable.restClient.downloadFilePattern")) {
                    downloadFilePattern = textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(configuration::getDowloadFilePattern, configuration::setDowloadFilePattern)
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                        .focused()
                }
                row(message("MidPointConfigurable.restClient.generatedFilePattern")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(configuration::getGeneratedFilePattern, configuration::setGeneratedFilePattern)
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                }
                row(message("MidPointConfigurable.restClient.timeout")) {
                    intTextField(IntRange(1, 3600), 1)
                        .bindIntText(configuration::getRestResponseTimeout, configuration::setRestResponseTimeout)
                }.comment(message("MidPointConfigurable.restClient.timeout.comment"))
                row {
                    checkBox(message("MidPointConfigurable.restClient.logCommunication"))
                        .bindSelected(
                            configuration::isPrintRestCommunicationToConsole,
                            configuration::setPrintRestCommunicationToConsole
                        )
                }
            }
            groupRowsRange(message("MidPointConfigurable.download.title")) {
                row(message("MidPointConfigurable.download.include")) {
                    expandableTextField()
                        .validationOnInput(::validateTypes)
                        .validationOnApply(::validateTypes)
                        .bindText(
                            { translateTypesToString(configuration.getDownloadTypesInclude()) },
                            { translateStringToTypes(it) })
                        .align(AlignX.FILL)
                }
                row(message("MidPointConfigurable.download.exclude")) {
                    expandableTextField()
                        .validationOnInput(::validateTypes)
                        .validationOnApply(::validateTypes)
                        .bindText(
                            { translateTypesToString(configuration.getDownloadTypesExclude()) },
                            { translateStringToTypes(it) })
                        .align(AlignX.FILL)
                }
                row(message("MidPointConfigurable.download.limit")) {
                    intTextField(IntRange(1, 500), 1)
                        .bindIntText(configuration::getTypesToDownloadLimit, configuration::setTypesToDownloadLimit)
                }
            }
        }
    }
}
