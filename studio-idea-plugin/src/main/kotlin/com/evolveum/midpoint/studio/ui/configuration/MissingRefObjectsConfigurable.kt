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

    private var configuration: MissingRefObjects

    init {
        configuration = loadConfiguration()

        editor = MissingRefObjectsEditor(project, configuration.objects)
    }

    override fun isModified(): Boolean {
        val service = CleanupService.get(project)

        return super.isModified() || !Objects.equals(configuration, service.settings.missingReferences)
    }

    override fun apply() {
        super.apply()

        val service = CleanupService.get(project)
        service.settings.missingReferences = configuration
    }

    override fun reset() {
        configuration = loadConfiguration()

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
                    listOf(
                        MissingRefAction.IGNORE,
                        MissingRefAction.DOWNLOAD
                    ),
                    SimpleListCellRenderer.create(
                        StudioLocalization.get().translate("ReferenceDecisionConfiguration.null")
                    ) { StudioLocalization.get().translateEnum(it) }
                )
                    .bindItem(
                        { configuration.defaultAction },
                        { configuration.defaultAction = it }
                    )
            }
                .rowComment(message("MissingReferencesConfigurable.defaultDecision.comment"))
        }
    }
}