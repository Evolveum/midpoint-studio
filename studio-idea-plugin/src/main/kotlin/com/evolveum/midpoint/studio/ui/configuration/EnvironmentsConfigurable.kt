package com.evolveum.midpoint.studio.ui.configuration


import com.evolveum.midpoint.studio.impl.EnvironmentService
import com.evolveum.midpoint.studio.util.Selectable
import com.evolveum.midpoint.studio.util.StudioBundle
import com.evolveum.midpoint.studio.util.StudioBundle.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import java.util.stream.Collectors

/**
 * Created by Viliam Repan (lazyman).
 */
class EnvironmentsConfigurable(val project: Project) :
    BoundSearchableConfigurable(message("EnvironmentsConfigurable.title"), "") {

    private val environmentsPanel = EnvironmentsPanel(project)

    override fun apply() {
        super.apply()
    }

    override fun isModified(): Boolean {
        return false
    }

    override fun reset() {
        val service = EnvironmentService.getInstance(project)
        val selected = service.selected?.id

        val list = EnvironmentService.getInstance(project).settings.environments
            .stream()
            .map { e ->
                val selectable = Selectable(e.copy())
                selectable.isSelected = e.id == selected
                selectable
            }
            .collect(
                Collectors.toList()
            )

        environmentsPanel.data = list
    }

    override fun createPanel(): DialogPanel {
        return panel {
            row() {
                cell(environmentsPanel)
                    .align(Align.FILL)
            }
                .resizableRow()
                .rowComment(StudioBundle.message("EnvironmentsConfigurable.environments.comment"))
        }
    }
}
