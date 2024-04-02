package com.evolveum.midpoint.studio.ui

import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefObjectsTable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel

class MissingRefObjectsEditor(val project: Project, val objects: List<MissingRefObject>) {

    private val table: MissingRefObjectsTable

    init {
        table = MissingRefObjectsTable()
        table.tableModel.data = objects
    }

    fun createComponent(): DialogPanel {
        return panel {
            // todo implement, probably action toolbar
            row {
                cell(JBScrollPane(table))
                    .align(Align.FILL)
            }
                .resizableRow()
        }
    }
}