package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.studio.impl.configuration.CleanupService
import com.evolveum.midpoint.studio.impl.configuration.MissingRefAction
import com.evolveum.midpoint.studio.impl.configuration.MissingRefObjects
import com.evolveum.midpoint.studio.ui.MissingRefObjectsEditor
import com.evolveum.midpoint.studio.util.StudioLocalization
import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import java.util.*

open class MissingRefObjectsConfigurable(val project: Project) :
    BoundSearchableConfigurable(message("MissingReferencesConfigurable.title"), "") {

    private val editor: MissingRefObjectsEditor

    private var defaultAction: MissingRefAction? = null

    init {
        val configuration = loadConfiguration()

        editor = MissingRefObjectsEditor(project)
        editor.setObjects(configuration.objects)
    }

    override fun isModified(): Boolean {
        val service = CleanupService.get(project)

        return super.isModified() || !Objects.equals(buildUpdatedSettings(), service.settings.missingReferences)
    }

    private fun buildUpdatedSettings(): MissingRefObjects {
        val result = MissingRefObjects()
        result.objects = editor.getObjects()
        result.defaultAction = defaultAction

        return result
    }

    override fun apply() {
        super.apply()

        val service = CleanupService.get(project)

        service.settings.missingReferences = buildUpdatedSettings()
        service.settingsUpdated()
    }

    override fun reset() {
        val configuration = loadConfiguration()

        editor.setObjects(configuration.objects)
        defaultAction = configuration.defaultAction
        if (defaultAction == null) {
            defaultAction = MissingRefAction.UNDEFINED
        }

        super.reset()
    }

    private fun loadConfiguration(): MissingRefObjects {
        val service = CleanupService.get(project)
        return service.settings.missingReferences.copy()
    }

    override fun createPanel(): DialogPanel {
        return panel {
            groupRowsRange(message("MissingReferencesConfigurable.missingObjects")) {
                row {
                    cell(editor.createComponent())
                        .align(Align.FILL)
                }
                    .resizableRow()
                    .rowComment(message("MissingReferencesConfigurable.missingObjects.comment"))
            }
            row(message("MissingReferencesConfigurable.defaultDecision")) {
                comboBox(
                    MissingRefAction.values().toList(),
                    SimpleListCellRenderer.create(
                        StudioLocalization.get().translate("ReferenceDecisionConfiguration.null")
                    ) { StudioLocalization.get().translateEnum(it) }
                )
                    .bindItem(
                        { defaultAction },
                        { defaultAction = it }
                    )
            }
                .rowComment(message("MissingReferencesConfigurable.defaultDecision.comment"))
        }
    }
}