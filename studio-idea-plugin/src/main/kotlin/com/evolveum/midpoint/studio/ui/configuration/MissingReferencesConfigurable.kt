package com.evolveum.midpoint.studio.ui.configuration

import com.evolveum.midpoint.studio.impl.configuration.*
import com.evolveum.midpoint.studio.ui.MissingObjectRefsEditor
import com.evolveum.midpoint.studio.ui.cleanup.MissingObjectRefsTable
import com.evolveum.midpoint.studio.util.StudioLocalization
import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel

open class MissingReferencesConfigurable(val project: Project) :
    BoundSearchableConfigurable(message("MissingReferencesConfigurable.title"), "") {

    private val configuration: MissingReferencesConfiguration

    private val editor: MissingObjectRefsEditor

    init {
        // todo remove, obtain it from cleanup service, implement apply/reset
        configuration = MissingReferencesConfiguration()
        configuration.action = DownloadActionConfiguration.ALWAYS

        val obj = ObjectReferencesConfiguration()
        obj.type = UserType.COMPLEX_TYPE
        obj.oid = "123"

        val ref = ReferenceConfiguration()
        ref.oid = "456"
        ref.type = ShadowType.COMPLEX_TYPE
        ref.decision = ReferenceDecisionConfiguration.DOWNLOAD

        configuration.objects.add(obj)

        editor = MissingObjectRefsEditor(project, configuration.objects)
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
                row(message("MissingReferencesConfigurable.defaultAction")) {
                    comboBox(
                        listOf(
                            DownloadActionConfiguration.NEVER,
                            DownloadActionConfiguration.ALWAYS,
                            DownloadActionConfiguration.OBJECT_NOT_AVAILABLE
                        ),
                        SimpleListCellRenderer.create(
                            StudioLocalization.get().translate("DownloadActionConfiguration.null")
                        ) { StudioLocalization.get().translateEnum(it) }
                    )
                        .bindItem(
                            { configuration.action },
                            { configuration.action = it }
                        )
                }
                    .rowComment(message("MissingReferencesConfigurable.defaultAction.comment"))
            }
        }
    }
}