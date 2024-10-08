package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.prism.path.ItemPath
import com.evolveum.midpoint.prism.schema.SchemaRegistry
import com.evolveum.midpoint.studio.impl.StudioPrismContextService
import com.evolveum.midpoint.studio.impl.configuration.CleanupPathActionConfiguration
import com.evolveum.midpoint.studio.impl.configuration.CleanupPathConfiguration
import com.evolveum.midpoint.studio.util.MidPointUtils
import com.evolveum.midpoint.studio.util.SchemaTypesProvider
import com.evolveum.midpoint.studio.util.StudioLocalization
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import javax.swing.JTextField

class CleanupPathConfigurationEditor(val project: Project, val input: CleanupPathConfiguration?) {

    val data: CleanupPathConfiguration =
        if (input != null)
            input.copy()
        else
            CleanupPathConfiguration(null, ItemPath.EMPTY_PATH, CleanupPathActionConfiguration.IGNORE)

    fun createComponent(): DialogPanel {
        val registry = StudioPrismContextService.getPrismContext(project).schemaRegistry
        val provider = SchemaTypesProvider(registry)
        val autoCompletion = TextFieldWithAutoCompletion(project, provider, false, null)

        return panel {
            row(StudioLocalization.message("CleanupEditorPanel.type")) {
                cell(autoCompletion)
                    .bind(
                        { it.text },
                        { it, value -> it.text = value },
                        MutableProperty(
                            { provider.stringToQName(data.type) },
                            { data.type = provider.qnameToString(it) }
                        )
                    )
                    .validationOnInput { component ->
                        validateValueExistence(
                            this,
                            component,
                            provider
                        )
                    }
                    .validationOnApply { component ->
                        validateValueExistence(
                            this,
                            component,
                            provider
                        )
                    }
                    .align(AlignX.FILL)
            }
            row(StudioLocalization.message("CleanupEditorPanel.path")) {
                textField()
                    .columns(COLUMNS_LARGE)
                    .bindText(
                        { if (data.path != null) data.path.toString() else "" },
                        { data.path = ItemPath.fromString(it) }
                    )
                    .validationOnInput(::validatePath)
                    .validationOnApply(::validatePath)
            }
            row(StudioLocalization.message("CleanupEditorPanel.action")) {
                comboBox(
                    CleanupPathActionConfiguration.values().toList(),
                    SimpleListCellRenderer.create("") { StudioLocalization.get().translateEnum(it) }
                )
                    .bindItem(
                        { data.action ?: CleanupPathActionConfiguration.IGNORE },
                        { data.action = it }
                    )
            }
        }
    }

    private fun validateValueExistence(
        builder: ValidationInfoBuilder,
        textField: TextFieldWithAutoCompletion<String>,
        provider: SchemaTypesProvider
    ): ValidationInfo? {
        return builder.run {
            val value = textField.text
            when {
                value.isBlank() -> error("Please fill in value")
                !provider.isValid(value) -> error("Invalid value")
                else -> null
            }
        }
    }

    private fun validatePath(builder: ValidationInfoBuilder, textField: JTextField): ValidationInfo? {
        return builder.run {
            val value = textField.text

            if (value.isNullOrBlank()) {
                return null
            }

            try {
                ItemPath.fromString(value)
            } catch (ex: Exception) {
                return error(ex.message ?: "Invalid item path")
            }

            return null
        }
    }
}
