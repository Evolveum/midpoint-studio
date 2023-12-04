package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.schema.constants.ObjectTypes
import com.evolveum.midpoint.studio.impl.configuration.CleanupPath
import com.evolveum.midpoint.studio.impl.configuration.CleanupPathAction
import com.evolveum.midpoint.studio.impl.service.MidPointLocalizationService
import com.evolveum.midpoint.studio.util.StudioBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class CleanupEditorPanel(val project: Project, val input: CleanupPath?) {

    private val realInput: CleanupPath = input ?: CleanupPath(ObjectTypes.OBJECT, "", CleanupPathAction.IGNORE)

    private val localizationService = MidPointLocalizationService.getInstance()

    var type: Cell<ComboBox<ObjectTypes>>? = null
    var path: Cell<JBTextField>? = null
    var action: Cell<ComboBox<CleanupPathAction>>? = null

    fun createComponent(): DialogPanel {
        return panel {
            row(StudioBundle.message("CleanupEditorPanel.type")) {
                type = comboBox(
                    sortTypes(),
                    SimpleListCellRenderer.create("") { translateEnum(it) }
                )
                    .bindItem({ realInput.type ?: ObjectTypes.OBJECT }, { })
                    .validationOnApply(::validateNotNull)
            }
            row(StudioBundle.message("CleanupEditorPanel.path")) {
                path = textField()
                    .bindText({ realInput.path ?: "" }, { })
                    .validationOnInput(::validateNotBlank)
                    .validationOnApply(::validateNotBlank)
            }
            row(StudioBundle.message("CleanupEditorPanel.action")) {
                action = comboBox(
                    CleanupPathAction.values().toList(),
                    SimpleListCellRenderer.create("") { translateEnum(it) }
                )
                    .bindItem({ realInput.action ?: CleanupPathAction.IGNORE }, { })
            }
        }
    }

    fun getData(): CleanupPath {
        return CleanupPath(
            type?.component?.selectedItem as ObjectTypes,
            path?.component?.text,
            action?.component?.selectedItem as CleanupPathAction
        )
    }

    private fun <X, T : Enum<X>> translateEnum(type: T?): String? {
        if (type == null) {
            return null
        }
        return localizationService.translateEnum(type)
    }

    private fun sortTypes(): List<ObjectTypes> {
        return ObjectTypes.values().toList().sortedBy { translateEnum(it) }
    }
}