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
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.text
import com.intellij.util.ui.JBFont

class ConnectorIdentificationStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Connector identification"
        ) {
            val connector = dataModel.connectorDevelopment.connector
            connector.groupId = connector.groupId?: "com.evolveum.polygon.community"
            connector.artifactId = connector.artifactId
                ?: dataModel.connectorDevelopment.application.applicationName.getNorm()
            connector.version = connector.version?: "1.0"

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
                    .text(dataModel.connectorDevelopment.connector?.groupId.orEmpty())
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
                    .text(dataModel.connectorDevelopment.connector?.artifactId.orEmpty())
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
                    .text(dataModel.connectorDevelopment.connector?.displayName?.orig.orEmpty())
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
                    .text(dataModel.connectorDevelopment.connector?.description.orEmpty())
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
                    .text(dataModel.connectorDevelopment.connector?.version.orEmpty())
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

        if (state == GenerateConnectorBadge.State.IN_PROGRESS ||
            state == GenerateConnectorBadge.State.EDITED
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
            dataModel.connectorDevelopment =
                upsertConnectorDevelopmentType(dataModel.connectorDevelopment)
            state = GenerateConnectorBadge.State.COMPLETE
        }

        super._commit(finishChosen)
    }
}