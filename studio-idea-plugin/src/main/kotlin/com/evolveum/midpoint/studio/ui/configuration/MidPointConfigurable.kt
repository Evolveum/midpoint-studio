package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.schema.constants.ObjectTypes
import com.evolveum.midpoint.studio.util.StudioBundle.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.ValidationInfoBuilder
import javax.swing.JTextField

// todo use ConfigurableProvider for this, only make it available if some module has midpoint facet
/**
 * Created by Viliam Repan (lazyman).
 */
open class MidPointConfigurable(

) : BoundSearchableConfigurable(message("MidPointConfigurable.title"), "") {

    var model: MidPointSettingsState

    init {
        model = loadModel()
    }

    open fun loadModel(): MidPointSettingsState {
        return MidPointSettingsState()
    }

    override fun apply() {
        val modified = isModified
        super.apply()

        if (modified) {
            MidPointSettings.getInstance().loadState(model)
        }
    }

    override fun createPanel(): DialogPanel {
        return panel {
            groupRowsRange(message("MidPointConfigurable.restClient.title")) {
                row(message("MidPointConfigurable.restClient.downloadFilePattern")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(model::downloadFilePattern)
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                }
                row(message("MidPointConfigurable.restClient.generatedFilePattern")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(model::generatedFilePattern)
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                }
                row(message("MidPointConfigurable.restClient.timeout")) {
                    intTextField(IntRange(1, 600), 1)
                        .bindIntText(model::restClientTimeout)
                }.comment(message("MidPointConfigurable.restClient.timeout.comment"))
                row {
                    checkBox(message("MidPointConfigurable.restClient.logCommunication"))
                        .bindSelected(model::restLogCommunication)
                }
            }
            groupRowsRange(message("MidPointConfigurable.download.title")) {
                row(message("MidPointConfigurable.download.include")) {
                    expandableTextField()
                        .validationOnInput(::validateTypes)
                        .validationOnApply(::validateTypes)
                        .bindText(
                            { translateTypesToString(model.downloadTypesInclude) },
                            { translateStringToTypes(it) })
                        .horizontalAlign(HorizontalAlign.FILL)
                }
                row(message("MidPointConfigurable.download.exclude")) {
                    expandableTextField()
                        .validationOnInput(::validateTypes)
                        .validationOnApply(::validateTypes)
                        .bindText(
                            { translateTypesToString(model.downloadTypesExclude) },
                            { translateStringToTypes(it) })
                        .horizontalAlign(HorizontalAlign.FILL)
                }
                row(message("MidPointConfigurable.download.limit")) {
                    intTextField(IntRange(1, 500), 1)
                        .bindIntText(model::downloadLimit)
                }
            }
        }   // this should be used with module wizard step .withVisualPadding(topField = true)
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
            .toList()
    }

    private fun validateTypes(builder: ValidationInfoBuilder, textField: JTextField): ValidationInfo? {
        return builder.run {
            val value = textField.text
            if (value.isNullOrBlank()) {
                return null;
            }

            val array = value?.split(",\\s") ?: emptyList()
            when {
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
