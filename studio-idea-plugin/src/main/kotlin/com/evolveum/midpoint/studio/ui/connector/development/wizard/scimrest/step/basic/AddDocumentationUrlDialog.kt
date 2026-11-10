/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.basic

import com.evolveum.midpoint.studio.ui.configuration.validateNotBlank
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

    private val panel: DialogPanel = panel {
        row("Name:") {
            textField()
                .columns(COLUMNS_LARGE)
                .bindText({ source.name.orEmpty() }, { source.name = it.trim() })
                .validationOnApply(::validateNotBlank)
                .focused()
        }
        row("Description:") {
            textField()
                .columns(COLUMNS_LARGE)
                .bindText({ source.description.orEmpty() }, { source.description = it.trim().ifEmpty { null } })
        }
        row("Content type:") {
            textField()
                .columns(COLUMNS_LARGE)
                .bindText({ source.contentType.orEmpty() }, { source.contentType = it.trim().ifEmpty { null } })
        }
        row("URI:") {
            textField()
                .columns(COLUMNS_LARGE)
                .bindText({ source.uri.orEmpty() }, { source.uri = it.trim() })
                .validationOnApply(::validateNotBlank)
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
}
