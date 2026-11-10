/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.basic

import com.evolveum.midpoint.studio.impl.MidPointClient

import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBFont

open class ApplicationIdentificationStep(
    wizardContext : ConnectorDevelopmentWizard,
    client : MidPointClient,
    state : Badge.State,
    isHeader : Boolean
): ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Application Identification"
        ) {
            row {
                cell(JBLabel("Identify the target application").apply {
                    font = JBFont.label().deriveFont(16f)
                })
            }

            row {
                text("""
                Tell us which application you want to connect to. Based on this information, the system will identify the target and locate appropriate documentation.
                """.trimIndent())
                    .align(AlignX.FILL)
            }

            separator()

            row("Application name *") {
                textField()
                    .align(AlignX.FILL)
                    .bindText(
                        { dataModel.connectorDevelopment.application?.applicationName?.orig.orEmpty() },
                        { value ->
                            dataModel.connectorDevelopment.application?.applicationName =
                                value.takeIf { it.isNotEmpty() }?.let { PolyStringType(it) }
                        }
                    )
            }

            row("Description") {
                scrollCell(textArea().apply {
                    component.rows = 5
                    component.lineWrap = true
                    component.wrapStyleWord = true
                    visible(false)
                }.component)
                    .align(Align.FILL)
                    .bindText(
                        { dataModel.connectorDevelopment.application?.description.orEmpty() },
                        { value ->
                            dataModel.connectorDevelopment.application?.description =
                                value.takeIf { it.isNotEmpty() }
                        }
                    )
            }

            row("Version of application") {
                textField()
                    .align(AlignX.FILL)
                    .bindText(
                        { dataModel.connectorDevelopment.application?.version.orEmpty() },
                        { value ->
                            dataModel.connectorDevelopment.application?.version =
                                value.takeIf { it.isNotEmpty() }
                        }
                    )
            }

            row("Integration Type *") {
                comboBox(
                    listOf(null) + ConnDevIntegrationType.entries,
                    textListCellRenderer(COMBO_BOX_ITEM_UNDEFINED) { "${it.name} - ${it.description()}" }
                ).bindItem(
                    {
                        dataModel.connectorDevelopment.application
                            ?.integrationType
                    },
                    { value ->
                        dataModel.connectorDevelopment.application.integrationType = value
                    }
                )
            }

            row("Deployment type") {
                comboBox(
                    listOf(null) + ConnDevDeploymentType.entries,
                    textListCellRenderer(COMBO_BOX_ITEM_UNDEFINED) { it.value().replaceFirstChar { char -> char.titlecase() } }
                ).bindItem(
                    {
                        dataModel.connectorDevelopment.application
                            ?.deploymentType
                    },
                    { value ->
                        dataModel.connectorDevelopment.application.deploymentType = value
                    }
                )
            }

            row("API Version") {
                textField()
                    .align(AlignX.FILL)
                    .bindText(
                        { dataModel.connectorDevelopment.application?.apiVersion.orEmpty() },
                        { value ->
                            dataModel.connectorDevelopment.application?.apiVersion =
                                value.takeIf { it.isNotEmpty() }
                        }
                    )
            }
        }
    }

    override fun _init() {
        super._init()

        if (dataModel.connectorDevelopment.application == null) {
            dataModel.connectorDevelopment.application = dataModel.connectorDevelopment.beginApplication()
        }

        dataModel.connectorDevelopment.testing

        originalConnectorDevelopmentType = dataModel.connectorDevelopment.clone()
        canGoNext = true
    }

    override fun _commit(finishChosen: Boolean) {

        dialogPanel.apply()

        dataModel.connectorDevelopment.application
            ?.applicationName
            ?.orig
            .requireNotBlank("Application name")

        dataModel.connectorDevelopment.application
            ?.integrationType?.value()
            .requireNotBlank("Integration Type")

        dataModel.connectorDevelopment.name =
            dataModel.connectorDevelopment.application.applicationName

        if ((state ==  Badge.State.IN_PROGRESS ||
            state == Badge.State.EDITED) &&
            !wizardContext.isLeavingStepByPreviousTouch
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
            updateConnectorDevelopment()
            state = Badge.State.COMPLETE
        }

        super._commit(finishChosen)
    }
}