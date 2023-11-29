package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.schema.constants.ObjectTypes
import com.evolveum.midpoint.studio.util.StudioBundle
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import javax.swing.JTextField

class MidpointConfigurationEditorPanel(val input: MidPointConfiguration) {

    fun createComponent(): DialogPanel {
        return panel {
            groupRowsRange(StudioBundle.message("MidPointConfigurable.restClient.title")) {
                row(StudioBundle.message("MidPointConfigurable.restClient.downloadFilePattern")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(input::getDownloadFilePattern, input::setDownloadFilePattern)
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                }
                row(StudioBundle.message("MidPointConfigurable.restClient.generatedFilePattern")) {
                    textField()
                        .columns(COLUMNS_LARGE)
                        .bindText(input::getGeneratedFilePattern, input::setGeneratedFilePattern)
                        .validationOnInput(::validateNotBlank)
                        .validationOnApply(::validateNotBlank)
                }
                row(StudioBundle.message("MidPointConfigurable.restClient.timeout")) {
                    intTextField(IntRange(1, 3600), 1)
                        .bindIntText(input::getRestClientTimeout, input::setRestClientTimeout)
                }.comment(StudioBundle.message("MidPointConfigurable.restClient.timeout.comment"))
                row {
                    checkBox(StudioBundle.message("MidPointConfigurable.restClient.logCommunication"))
                        .bindSelected(input::getRestLogCommunication, input::setRestLogCommunication)
                }
            }
            groupRowsRange(StudioBundle.message("MidPointConfigurable.download.title")) {
                row(StudioBundle.message("MidPointConfigurable.download.include")) {
                    expandableTextField()
                        .validationOnInput(::validateTypes)
                        .validationOnApply(::validateTypes)
                        .bindText(
                            { translateTypesToString(input.getDownloadTypesInclude()) },
                            { translateStringToTypes(it) })
                        .align(AlignX.FILL)
                }
                row(StudioBundle.message("MidPointConfigurable.download.exclude")) {
                    expandableTextField()
                        .validationOnInput(::validateTypes)
                        .validationOnApply(::validateTypes)
                        .bindText(
                            { translateTypesToString(input.getDownloadTypesExclude()) },
                            { translateStringToTypes(it) })
                        .align(AlignX.FILL)
                }
                row(StudioBundle.message("MidPointConfigurable.download.limit")) {
                    intTextField(IntRange(1, 500), 1)
                        .bindIntText(input::getDownloadLimit, input::setDownloadLimit)
                }
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

    fun getData(): MidPointConfiguration {
        return MidPointConfiguration(

        )
    }
}
