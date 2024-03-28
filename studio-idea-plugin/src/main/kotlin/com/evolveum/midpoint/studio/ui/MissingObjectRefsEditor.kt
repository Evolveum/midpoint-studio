package com.evolveum.midpoint.studio.ui

import com.evolveum.midpoint.studio.impl.configuration.ObjectReferencesConfiguration
import com.evolveum.midpoint.studio.ui.cleanup.MissingObjectRefsTable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel

class MissingObjectRefsEditor(val project: Project, val objects: List<ObjectReferencesConfiguration>) {

    private val table: MissingObjectRefsTable

    init {
        table = MissingObjectRefsTable()
        table.tableModel.data = objects
    }

    fun createComponent(): DialogPanel {
        return panel {
            // todo implement
            row {
                cell(JBScrollPane(table))
                    .align(Align.FILL)
            }
                .resizableRow()
        }
    }
}