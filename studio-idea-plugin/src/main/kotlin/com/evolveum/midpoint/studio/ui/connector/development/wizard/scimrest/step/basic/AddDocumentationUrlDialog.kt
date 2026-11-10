/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.basic

import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDocumentationSourceType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

/**
 * Collects a manually entered documentation source (URL) for the documentation step.
 */
class AddDocumentationUrlDialog(project: Project) : DialogWrapper(project) {

    private val source = ConnDevDocumentationSourceType()

    // IntelliJ guidelines: required fields are not marked, optional ones carry an "Optional" placeholder,
    // empty required fields are reported when OK is pressed
    private val panel: DialogPanel = panel {
        row("Name:") {
            textField()
                .columns(COLUMNS_LARGE)
                .bindText({ source.name.orEmpty() }, { source.name = it.trim() })
                .validationOnApply { if (it.text.isBlank()) error("Specify the name") else null }
                .focused()
        }
        row("Description:") {
            textField()
                .columns(COLUMNS_LARGE)
                .applyToComponent { emptyText.text = OPTIONAL }
                .bindText({ source.description.orEmpty() }, { source.description = it.trim().ifEmpty { null } })
        }
        row("Content type:") {
            textField()
                .columns(COLUMNS_LARGE)
                .applyToComponent { emptyText.text = OPTIONAL }
                .bindText({ source.contentType.orEmpty() }, { source.contentType = it.trim().ifEmpty { null } })
        }
        row("URI:") {
            textField()
                .columns(COLUMNS_LARGE)
                .bindText({ source.uri.orEmpty() }, { source.uri = it.trim() })
                .validationOnApply { if (it.text.isBlank()) error("Specify the URI") else null }
        }
    }

    init {
        title = "Add New Documentation"
        init()
    }

    override fun createCenterPanel(): JComponent = panel

    override fun doValidateAll(): List<ValidationInfo> = panel.validateAll()

    override fun doOKAction() {
        panel.apply()
        super.doOKAction()
    }

    fun showAndGetSource(): ConnDevDocumentationSourceType? = if (showAndGet()) source else null

    companion object {
        private const val OPTIONAL = "Optional"
    }
}
