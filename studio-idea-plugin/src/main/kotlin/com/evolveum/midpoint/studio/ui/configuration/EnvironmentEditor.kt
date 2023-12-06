package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.studio.client.ProxyType
import com.evolveum.midpoint.studio.impl.Environment
import com.evolveum.midpoint.studio.impl.service.MidPointLocalizationService
import com.evolveum.midpoint.studio.util.MidPointUtils
import com.evolveum.midpoint.studio.util.Selectable
import com.evolveum.midpoint.studio.util.StudioBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent
import javax.swing.JLabel

class EnvironmentEditor(val project: Project, val input: Selectable<Environment>?) {

    private val localizationService = MidPointLocalizationService.getInstance()

    private val selectable: Selectable<Environment>

    init {
        if (input != null) {
            selectable = input
        } else {
            selectable = Selectable(Environment())
            selectable.getObject().awtColor = MidPointUtils.generateAwtColor()
        }
    }

    private var name: Cell<JBTextField>? = null
    private var color: Cell<JLabel>? = null
    private var testConnectionResult: Cell<JLabel>? = null

    fun getPreferredFocusedComponent(): JComponent? {
        return name?.component
    }

    fun createComponent(): DialogPanel {
        return panel {
            groupRowsRange(StudioBundle.message("EnvironmentEditorPanel.general")) {
                row(StudioBundle.message("EnvironmentEditorPanel.name")) {
                    name = textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(selectable.`object`::getName, selectable.`object`::setName)
                        .validationOnApply(::validateNotBlank)
                        .validationOnInput(::validateNotBlank)
                        .focused()
                }
                row {
                    checkBox(StudioBundle.message("EnvironmentEditorPanel.selected"))
                        .bindSelected(selectable::isSelected, selectable::setSelected)
                }
            }
            groupRowsRange(StudioBundle.message("EnvironmentEditorPanel.server")) {
                row(StudioBundle.message("EnvironmentEditorPanel.server.url")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(selectable.`object`::getUrl, selectable.`object`::setUrl)
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                }
                row(StudioBundle.message("EnvironmentEditorPanel.server.username")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(selectable.`object`::getUsername, selectable.`object`::setUsername)
                }
                row(StudioBundle.message("EnvironmentEditorPanel.server.password")) {
                    passwordField()
                        .columns(COLUMNS_LARGE)
                        .bindText(selectable.`object`::getPassword, selectable.`object`::setPassword)
                }
                row {
                    checkBox(StudioBundle.message("EnvironmentEditorPanel.server.ignoreSslErrors"))
                        .bindSelected(selectable::isSelected, selectable::setSelected)
                }
            }
            groupRowsRange(StudioBundle.message("EnvironmentEditorPanel.proxy")) {
                row(StudioBundle.message("EnvironmentEditorPanel.proxy.host")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(selectable.`object`::getProxyServerHost, selectable.`object`::setProxyServerHost)
                }
                row(StudioBundle.message("EnvironmentEditorPanel.proxy.port")) {
                    intTextField(IntRange(0, 65535), 1)
//                        .bindIntText(selectable.`object`::getProxyServerPort , selectable.`object`::setProxyServerPort)
                }
                row(StudioBundle.message("EnvironmentEditorPanel.proxy.type")) {
                    // todo missing combo with proxy type
                    comboBox(
                        ProxyType.values().toList(),
                        SimpleListCellRenderer.create("") { localizationService.translateEnum(it) }
                    )
                }
                row(StudioBundle.message("EnvironmentEditorPanel.proxy.username")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(selectable.`object`::getProxyUsername, selectable.`object`::setProxyUsername)
                }
                row(StudioBundle.message("EnvironmentEditorPanel.proxy.password")) {
                    passwordField()
                        .columns(COLUMNS_LARGE)
                        .bindText(selectable.`object`::getProxyPassword, selectable.`object`::setProxyPassword)
                }
            }
            groupRowsRange(StudioBundle.message("EnvironmentEditorPanel.other")) {
                row {
                    checkBox(StudioBundle.message("EnvironmentEditorPanel.other.http2"))
                        .bindSelected(selectable.`object`::isUseHttp2, selectable.`object`::setUseHttp2)
                }
                row(StudioBundle.message("EnvironmentEditorPanel.other.properties")) {
                    textFieldWithBrowseButton(
                        StudioBundle.message("EnvironmentEditorPanel.other.properties.title"),
                        project,
                        FileChooserDescriptorFactory.createSingleFileDescriptor("properties")
                    )
                        .columns(COLUMNS_LARGE)

                }
                row(StudioBundle.message("EnvironmentEditorPanel.other.color")) {
                    // todo color
                    color = label("")
                }
                row {
                    // todo test connection result
                    testConnectionResult = label("")
                }
            }
        }
    }
}
