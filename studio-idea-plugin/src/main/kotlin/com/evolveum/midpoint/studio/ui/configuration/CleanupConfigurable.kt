package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.studio.impl.configuration.CleanupPathActionConfiguration
import com.evolveum.midpoint.studio.impl.configuration.CleanupService
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
import java.util.stream.Collectors

class CleanupConfigurable(val project: Project) :
    BoundSearchableConfigurable(message("CleanupConfigurable.title"), "") {

    private val cleanupPathsPanel = CleanupPathsPanel(project)

    private var askActionOverride: CleanupPathActionConfiguration? = null

    override fun apply() {
        super.apply()

        val list = cleanupPathsPanel.data

        val service = CleanupService.getInstance(project)
        val settings = service.settings

        settings.cleanupPaths = list
        settings.askActionOverride = askActionOverride

        service.settingsUpdated()
    }

    override fun isModified(): Boolean {
        val service = CleanupService.getInstance(project)
        val original = service.settings.cleanupPaths

        return super.isModified() || !Objects.equals(original, cleanupPathsPanel.data)
    }

    override fun reset() {
        val service = CleanupService.getInstance(project)
        val settings = service.settings

        val list = settings.cleanupPaths.stream()
            .map { p -> p.copy() }
            .collect(Collectors.toList())

        cleanupPathsPanel.data = list
        askActionOverride = settings.askActionOverride

        super.reset()
    }

    override fun createPanel(): DialogPanel {
        return panel {
            row {
                cell(cleanupPathsPanel)
                    .align(Align.FILL)
            }
                .resizableRow()
                .rowComment(message("CleanupConfigurable.cleanupPaths.comment"))
            row(message("CleanupConfigurable.askActionOverride")) {
                comboBox(
                    listOf(
                        null,
                        CleanupPathActionConfiguration.IGNORE,
                        CleanupPathActionConfiguration.REMOVE
                    ),
                    SimpleListCellRenderer.create(StudioLocalization.get().translate("CleanupConfigurable.nullGlobalAction")) { StudioLocalization.get().translateEnum(it) }
                )
                    .bindItem(
                        { askActionOverride },
                        { askActionOverride = it }
                    )
            }
        }
    }
}
