package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.studio.impl.configuration.CleanupConfiguration
import com.evolveum.midpoint.studio.impl.configuration.CleanupPathActionConfiguration
import com.evolveum.midpoint.studio.impl.configuration.CleanupService
import com.evolveum.midpoint.studio.util.MidPointUtils
import com.evolveum.midpoint.studio.util.StudioLocalization
import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import java.util.*

class CleanupConfigurable(val project: Project) :
    BoundSearchableConfigurable(message("CleanupConfigurable.title"), "") {

    private var configuration: CleanupConfiguration = CleanupConfiguration()

    private val cleanupPathsPanel = CleanupPathsPanel(project)

    override fun apply() {
        super.apply()

        val service = CleanupService.get(project)
        service.settings = configuration
    }

    override fun isModified(): Boolean {
        val service = CleanupService.get(project)

        return super.isModified() || !Objects.equals(configuration, service.settings)
    }

    override fun reset() {
        val service = CleanupService.get(project)
        configuration = service.settings.copy()

        cleanupPathsPanel.data = configuration.cleanupPaths

        super.reset()
    }

    override fun createPanel(): DialogPanel {
        return panel {
            groupRowsRange(message("CleanupConfigurable.paths")) {
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
                        SimpleListCellRenderer.create(
                            StudioLocalization.get().translate("CleanupConfigurable.nullGlobalAction")
                        ) { StudioLocalization.get().translateEnum(it) }
                    )
                        .bindItem(
                            { configuration.askActionOverride },
                            { configuration.askActionOverride = it }
                        )
                }
            }
            groupRowsRange(message("CleanupConfigurable.references")) {
                row {
                    checkBox(message("CleanupConfigurable.cleanupConnectorReferences"))
                        .comment(message("CleanupConfigurable.cleanupConnectorReferences.comment"))
                        .bindSelected(
                            { configuration.isCleanupConnectorReferences },
                            { configuration.isCleanupConnectorReferences = it }
                        )
                }
                row {
                    checkBox(message("CleanupConfigurable.replaceConnectorReferences"))
                        .comment(message("CleanupConfigurable.replaceConnectorReferences.comment"))
                        .bindSelected(
                            { configuration.isReplaceConnectorOidsWithFilter },
                            { configuration.isReplaceConnectorOidsWithFilter = it }
                        )
                }
                row {
                    checkBox(message("CleanupConfigurable.warnAboutMissingOid"))
                        .comment(message("CleanupConfigurable.warnAboutMissingOid.comment"))
                        .bindSelected(
                            { configuration.isWarnAboutMissingReferences },
                            { configuration.isWarnAboutMissingReferences = it }
                        )
                }
            }
            groupRowsRange(message("CleanupConfigurable.other")) {
                row {
                    checkBox(message("CleanupConfigurable.missingNaturalKeys"))
                        .comment(message("CleanupConfigurable.missingNaturalKeys.comment"))
                        .bindSelected(
                            { configuration.isMissingNaturalKeys },
                            { configuration.isMissingNaturalKeys = it }
                        )
                }
                // todo disabled for now since we have to decide on approach to PCV IDs and cleanup/storing in vcs
                //  also check CleanupService (development switch also there)
                row {
                    checkBox(message("CleanupConfigurable.removeContainerIds"))
                        .comment(message("CleanupConfigurable.removeContainerIds.comment"))
                        .bindSelected(
                            { configuration.isRemoveContainerIds },
                            { configuration.isRemoveContainerIds = it }
                        )
                }
                    .visible(MidPointUtils.isDevelopmentMode(true))
            }
        }
    }
}
