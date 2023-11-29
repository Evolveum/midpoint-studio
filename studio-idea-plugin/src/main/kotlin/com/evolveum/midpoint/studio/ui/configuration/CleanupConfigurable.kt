package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.studio.impl.configuration.CleanupService
import com.evolveum.midpoint.studio.util.StudioBundle
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import java.util.*
import java.util.stream.Collectors

class CleanupConfigurable(val project: Project) :
    BoundSearchableConfigurable(StudioBundle.message("CleanupConfigurable.title"), "") {

    private val cleanupPathsPanel = CleanupPathsPanel(project)

    override fun apply() {
        val list = cleanupPathsPanel.data

        CleanupService.getInstance(project).settings.cleanupPaths = list
    }

    override fun reset() {
        val list = CleanupService.getInstance(project).settings.cleanupPaths.stream().map { p -> p.copy() }.collect(
            Collectors.toList()
        )

        cleanupPathsPanel.data = list
    }

    override fun isModified(): Boolean {
        val list = cleanupPathsPanel.data

        return !Objects.equals(CleanupService.getInstance(project).settings.cleanupPaths, list)
    }

    override fun createPanel(): DialogPanel {
        return panel {
            row() {
                cell(cleanupPathsPanel)
                    .align(Align.FILL)
            }
                .resizableRow()
                .rowComment(StudioBundle.message("CleanupConfigurable.cleanupPaths.comment"))
        }
    }
}
