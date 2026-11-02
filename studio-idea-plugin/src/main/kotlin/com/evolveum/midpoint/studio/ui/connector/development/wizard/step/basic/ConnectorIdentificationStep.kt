/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.step.basic

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.wizard.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.step.basic.ApplicationIdentificationStep.Companion.COMBO_BOX_ITEM_UNDEFINED
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevConnectorIntentType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.util.ui.JBFont
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.ListCellRenderer

class ConnectorIdentificationStep(
    wizardContext : ConnectorDevelopmentWizard,
    client : MidPointClient,
    state : Badge.State,
    isHeader : Boolean
) : ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Connector identification"
        ) {
            val connector = dataModel.connectorDevelopment.connector
            connector.groupId = connector.groupId?: "com.evolveum.polygon.community"
            connector.artifactId = connector.artifactId
                ?: dataModel.connectorDevelopment.application.applicationName.getNorm()
            connector.version = connector.version?: "1.0-SNAPSHOT"

            row {
                cell(JBLabel("Set Basic Information About the Connector").apply {
                    font = JBFont.label().deriveFont(16f)
                })
            }

            row {
                text("""
                    On this panel you can enter the fundamental details that describe the connector, providing the necessary context before adding more specific configuration.
                    """.trimIndent())
                    .align(AlignX.FILL)
            }

            separator()

            row("Group id *") {
                textField()
                    .align(AlignX.FILL)
                    .bindText(
                        { dataModel.connectorDevelopment.connector?.groupId.orEmpty() },
                        { value ->
                            dataModel.connectorDevelopment.connector?.groupId = value.takeIf { it.isNotEmpty() }
                        }
                    )
            }

            row("Artifact id *") {
                textField()
                    .align(AlignX.FILL)
                    .bindText(
                        { dataModel.connectorDevelopment.connector?.artifactId.orEmpty() },
                        { value ->
                            dataModel.connectorDevelopment.connector?.artifactId = value.takeIf { it.isNotEmpty() }
                        }
                    )
            }

            row("Display name") {
                textField()
                    .align(AlignX.FILL)
                    .bindText(
                        { dataModel.connectorDevelopment.connector?.displayName?.orig.orEmpty() },
                        { value ->
                            dataModel.connectorDevelopment.connector?.displayName =
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
                        { dataModel.connectorDevelopment.connector?.description.orEmpty() },
                        { value ->
                            dataModel.connectorDevelopment.connector?.description = value.takeIf { it.isNotEmpty() }
                        }
                    )
            }

            row("Version *") {
                textField()
                    .align(AlignX.FILL)
                    .bindText(
                        { dataModel.connectorDevelopment.connector?.version.orEmpty() },
                        { value ->
                            dataModel.connectorDevelopment.connector?.version = value.takeIf { it.isNotEmpty() }
                        }
                    )
            }

            row("Integration Type *") {
                comboBox(
                    listOf(null) + ConnDevIntegrationType.entries,
                    ListCellRenderer<ConnDevIntegrationType?> { list, value, _, isSelected, _ ->
                        JLabel(
                            when (value) {
                                null -> COMBO_BOX_ITEM_UNDEFINED
                                else -> "${value.name} - ${value.description()}"
                            }
                        ).apply {
                            isOpaque = true
                            background = if (isSelected) list.selectionBackground else list.background
                            foreground = if (isSelected) list.selectionForeground else list.foreground
                            border = BorderFactory.createEmptyBorder(2, 4, 2, 4)
                        }
                    }
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

            row("Intent") {
                comboBox(
                    listOf(null) + ConnDevConnectorIntentType.entries,
                    ListCellRenderer<ConnDevConnectorIntentType?> { list, value, _, isSelected, _ ->
                        JLabel(
                            when (value) {
                                null -> COMBO_BOX_ITEM_UNDEFINED
                                else -> "${value.name} - ${value.description()}"
                            }
                        ).apply {
                            isOpaque = true
                            background = if (isSelected) list.selectionBackground else list.background
                            foreground = if (isSelected) list.selectionForeground else list.foreground
                            border = BorderFactory.createEmptyBorder(2, 4, 2, 4)
                        }
                    }
                ).bindItem(
                    {
                        dataModel.connectorDevelopment.connector?.intent
                    },
                    { value ->
                        dataModel.connectorDevelopment.connector.intent = value
                    }
                )
            }
        }
    }

    override fun _init() {
        super._init()

        originalConnectorDevelopmentType = dataModel.connectorDevelopment.clone()

        canGoNext = true
    }

    override fun _commit(finishChosen: Boolean) {

        dialogPanel.apply()

        val connector = dataModel.connectorDevelopment.connector
            ?: throw CommitStepException("Connector Development object missing")

        connector.groupId.requireNotBlank("Group Id")
        connector.artifactId.requireNotBlank("Artifact Id")
        connector.version.requireNotBlank("Version")

        connector.integrationType = dataModel.connectorDevelopment.application.integrationType

        dataModel.connectorDevelopment.name = PolyStringType.fromOrig(
            (connector.groupId
                    + ":" + connector.artifactId
                    + ":" + connector.version)
        )

        if (state == Badge.State.IN_PROGRESS ||
            state == Badge.State.EDITED
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
            updateConnectorDevelopment()
            state = Badge.State.COMPLETE
        }

        super._commit(finishChosen)
    }
}