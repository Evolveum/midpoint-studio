/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.step.basic

import com.evolveum.midpoint.studio.impl.MidPointClient

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBFont

class ApplicationIdentificationStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
): ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {

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
                    .text(dataModel.connectorDevelopment.application?.applicationName?.orig.orEmpty())
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
                    .text(dataModel.connectorDevelopment.application?.description.orEmpty())
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
                    .text(dataModel.connectorDevelopment.application?.version.orEmpty())
            }

            row("Integration Type *") {
                comboBox(
                    listOf(COMBO_BOX_ITEM_UNDEFINED) + ConnDevIntegrationType.entries.map { it.name }
                ).bindItem(
                    {
                        dataModel.connectorDevelopment.application
                            ?.integrationType
                            ?.name
                            ?: COMBO_BOX_ITEM_UNDEFINED
                    },
                    { value ->
                        dataModel.connectorDevelopment.application.integrationType =
                            value.takeUnless { it == COMBO_BOX_ITEM_UNDEFINED }
                                ?.let { ConnDevIntegrationType.valueOf(it) }
                    }
                )
            }

            row("Deployment type") {
                comboBox(
                    listOf(COMBO_BOX_ITEM_UNDEFINED) + ConnDevDeploymentType.entries.map { it.name }
                ).bindItem(
                    {
                        dataModel.connectorDevelopment.application
                            ?.deploymentType
                            ?.name
                            ?: COMBO_BOX_ITEM_UNDEFINED
                    },
                    { value ->
                        dataModel.connectorDevelopment.application.deploymentType =
                            value.takeUnless { it == COMBO_BOX_ITEM_UNDEFINED }
                                ?.let { ConnDevDeploymentType.valueOf(it) }
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

        // FIXME found out why this is necessary (otherwise testing is null without initialization)
        dataModel.connectorDevelopment.testing

        originalConnectorDevelopmentType = dataModel.connectorDevelopment.clone()
        canGoNext = true
    }

    override fun _commit(finishChosen: Boolean) {

        dialogPanel.apply()

        dataModel.connectorDevelopment.application
            ?.applicationName
            ?.orig
            .requireNotBlank("Integration Type")

        dataModel.connectorDevelopment.application
            ?.integrationType?.value()
            ?.takeUnless { it == COMBO_BOX_ITEM_UNDEFINED }
            .requireNotBlank("Integration Type")

        dataModel.connectorDevelopment.name =
            dataModel.connectorDevelopment.name
                ?: dataModel.connectorDevelopment.application.applicationName

        if ((state ==  GenerateConnectorBadge.State.IN_PROGRESS ||
            state == GenerateConnectorBadge.State.EDITED) &&
            !wizardContext.isLeavingStepByPreviousTouch
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
            dataModel.connectorDevelopment =
                upsertConnectorDevelopmentType(dataModel.connectorDevelopment)

            state = GenerateConnectorBadge.State.COMPLETE
        }

        super._commit(finishChosen)
    }

    companion object {
        private const val COMBO_BOX_ITEM_UNDEFINED = "Undefined"
    }
}