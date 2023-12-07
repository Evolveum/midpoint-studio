package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.prism.path.ItemPath
import com.evolveum.midpoint.studio.impl.configuration.CleanupPathActionConfiguration
import com.evolveum.midpoint.studio.impl.configuration.CleanupPathConfiguration
import com.evolveum.midpoint.studio.util.StudioLocalization
import com.evolveum.midpoint.studio.util.MidPointUtils
import com.evolveum.midpoint.studio.util.SchemaTypesProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder

class CleanupPathConfigurationEditor(val project: Project, val input: CleanupPathConfiguration?) {

    val data: CleanupPathConfiguration =
        if (input != null)
            input.copy()
        else
            CleanupPathConfiguration(null, ItemPath.EMPTY_PATH, CleanupPathActionConfiguration.IGNORE)

    fun createComponent(): DialogPanel {
        val provider = SchemaTypesProvider(MidPointUtils.DEFAULT_PRISM_CONTEXT.schemaRegistry)
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
                    .validationOnInput(::validateItemPath)
                    .validationOnApply(::validateItemPath)
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
}
