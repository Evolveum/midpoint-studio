package com.evolveum.midpoint.studio.ui

import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefObjectsPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel

class MissingRefObjectsEditor(val project: Project, val objects: List<MissingRefObject>) {

    fun createComponent(): DialogPanel {
        return panel {
            row {
                cell(MissingRefObjectsPanel(objects))
                    .align(Align.FILL)
            }
                .resizableRow()
        }
    }
}