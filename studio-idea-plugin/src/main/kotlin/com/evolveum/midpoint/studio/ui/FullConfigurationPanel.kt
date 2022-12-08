package com.evolveum.midpoint.studio.ui

import com.evolveum.midpoint.schema.constants.ObjectTypes
import com.evolveum.midpoint.studio.impl.EnvironmentSettings
import com.evolveum.midpoint.studio.impl.MidPointSettings
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.ValidationInfoBuilder
import javax.swing.JTextField
import javax.swing.ListCellRenderer

import com.evolveum.midpoint.studio.util.MidPointBundle.message

/**
 * Created by Viliam Repan (lazyman).
 */
open class FullConfigurationPanel(
    val project: Project,
    val model: GeneralConfiguration,
    val midpointSettings: MidPointSettings,
    val environmentSettings: EnvironmentSettings
) {

    fun createPanel(): DialogPanel {
        return panel {
            groupRowsRange(message("midpoint.configuration.general.group.title")) {
                row(message("midpoint.configuration.general.midpointModule")) {
                    comboBox(
                        listOfModules(),
                        modulesRenderer()
                    )
                        .bindItemNullable(model::midpointModule)
                }.comment(message("midpoint.configuration.general.midpointModule.comment"))
                row(message("midpoint.configuration.general.importFormEclipse")) {
                    button("Import") {
                        onImportFromEclipseClicked()
                    }
                }.comment("Import project created using Eclipse with old Midpoint plugin")
            }
            groupRowsRange("REST client") {
                row("Download file pattern:") {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(model::downloadFilePattern)
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                }
                row("Generated file pattern:") {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(model::generatedFilePattern)
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                }
                row("REST client timeout [s]:") {
                    intTextField(IntRange(1, 600), 1)
                        .bindIntText(model::restClientTimeout)
                }
                row {
                    checkBox("Log REST communication")
                        .bindSelected(model::restLogCommunication)
                }
            }
            collapsibleGroup("Types to download") {
                row("Include:") {
                    expandableTextField()
                        .validationOnInput(::validateTypes)
                        .validationOnApply(::validateTypes)
                        .bindText({translateTypesToString(model.downloadTypesInclude)}, { translateStringToTypes(it) })
                        .horizontalAlign(HorizontalAlign.FILL)
                }
                row("Exclude:") {
                    expandableTextField()
                        .validationOnInput(::validateTypes)
                        .validationOnApply(::validateTypes)
                        .bindText({translateTypesToString(model.downloadTypesExclude)}, { translateStringToTypes(it) })
                        .horizontalAlign(HorizontalAlign.FILL)
                }
                row("Download limit:") {
                    intTextField(IntRange(1, 500), 1)
                        .bindIntText(model::downloadLimit)
                }
            }
            collapsibleGroup("Master password") {
                row("Old password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                }
                row("New password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                }
                row("Repeat new password:") {
                    cell(JBPasswordField())
                        .columns(COLUMNS_SHORT)
                }
            }
            collapsibleGroup("Environments") {
                row {
                    cell(EnvironmentsPanel(project, midpointSettings, environmentSettings))
                        .horizontalAlign(HorizontalAlign.FILL)
                }
            }
        }
    }

    private fun listOfModules(): Collection<Module?> {
        val manager = ModuleManager.getInstance(project)

        val list:MutableList<Module?> = manager.modules.toMutableList()
        list.sortWith(compareBy { it?.name })
        list.add(0, null)

        return list
    }

    private fun modulesRenderer(): ListCellRenderer<in Module?> {
        return SimpleListCellRenderer.create("", { it?.name })
    }

    open fun onImportFromEclipseClicked() {

    }

    private fun validateNotBlank(builder: ValidationInfoBuilder, textField: JTextField): ValidationInfo? {
        return builder.run {
            val value = textField.text
            when {
                value.isNullOrBlank() -> error("Please fill in value")
                else -> null
            }
        }
    }

    private fun translateTypesToString(list: List<ObjectTypes>): String {
        if (list.isEmpty()) {
            return ""
        }

        return list.joinToString { it.value }
    }

    private fun translateStringToTypes(value: String): List<ObjectTypes> {
        if (value.isEmpty()) {
            return emptyList()
        }

        return value.split("[,\\s]").mapNotNull { translateToObjectType(it) }
            .toList() as List<ObjectTypes>
    }

    private fun validateTypes(builder: ValidationInfoBuilder, textField: JTextField): ValidationInfo? {
        return builder.run {
            val value = textField.text

            val array = value?.split("[,\\s]") ?: emptyList()
            when {
                value.isNullOrBlank() -> null
                array.map { translateToObjectType(it) }.none { it == null } -> null
                else -> error("Unknown types: " + array.filter { translateToObjectType(it) != null }.toList())
            }
        }
    }

    private fun translateToObjectType(item: String): ObjectTypes? {
        if (item.isBlank()) {
            return null
        }

        for (type in ObjectTypes.values()) {
            if (type.value.equals(item)) {
                return type
            }
        }

        return null
    }
}
