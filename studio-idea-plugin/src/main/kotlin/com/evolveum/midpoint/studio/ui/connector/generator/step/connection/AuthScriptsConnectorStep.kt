/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.step.connection

import com.evolveum.midpoint.studio.client.RPCOperation
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.concurrency.EdtExecutorService
import com.intellij.util.ui.JBFont
import org.jetbrains.plugins.groovy.GroovyLanguage
import javax.swing.JPanel

class AuthScriptsConnectorStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Auth Scripts Connector"
        ) { }
    }

    override fun _init() {
        super._init()

        if (!wizardContext.isLeavingStepByPreviousTouch) {
            submitOperation()
        }
    }

    override fun _commit(finishChosen: Boolean) {
        super._commit(finishChosen)

        if (state == GenerateConnectorBadge.State.IN_PROGRESS ||
            state == GenerateConnectorBadge.State.EDITED
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
            state = GenerateConnectorBadge.State.COMPLETE
        }
    }

    fun createPanel(content: String): JPanel = panel {

        row {
            cell(JBLabel("Authentication script validation").apply {
                font = JBFont.label().deriveFont(16f)
            })
        }

        row {
            text("""
            This step involves examining the authentication script to ensure it functions correctly and securely. You can review the logic, validate credentials handling, and make adjustments to the script if anything seems incorrect or could be improved.
            """.trimIndent())
                .align(AlignX.FILL)
        }.bottomGap(BottomGap.MEDIUM)

        separator()

        row {
            val smartEditorComponent = SmartEditorComponent(client.project)
            smartEditorComponent.setText(content, GroovyLanguage)
            cell(smartEditorComponent).align(Align.FILL)
        }
    }

    private fun submitOperation() {

        replaceContent(
            getLoadingComponent(
                statusPanel,
                "Generating Authentication Script...",
                """
                We're creating a functional authentication script based on the authentication methods supported by system. This usually takes a few seconds.
                """.trimIndent()
            )
        )

        submitOperation(
            {
                client.submitOperationConnGenerator(
                    RPCOperation.RPC_GENERATE_AUTHENTICATION_SCRIPT,
                    mapOf(
                        "oid" to dataModel.connectorDevelopment.oid,
                        "retry" to false
                    ),
                    null
                )
            },
            { token ->
                client.getStatusInfoConnGenerator(
                    RPCOperation.RPC_GENERATE_AUTHENTICATION_SCRIPT,
                    token
                )
            },
            client.project,
            "Generate Auth Script connector",
            true
        ).thenAcceptAsync(
            { statusInfo ->

                statusPanel.elapsedLabel?.stop()

                if (statusInfo.status == OperationResultStatusType.SUCCESS) {
                    val content = statusInfo.result?.connDevGenerateArtifactResult?.artifact?.content?: run {
                        statusPanel.status = StatusPanel.Status.ERROR
                        replaceContent(
                            getAlertComponent(
                                statusPanel,
                                statusPanel.status?.name?: "",
                                "Failed to generate authentication script",
                            )
                        )

                        return@thenAcceptAsync
                    }

                    dynamicPanel.removeAll()
                    dynamicPanel.add(createPanel(content))
                    dynamicPanel.revalidate()
                    dynamicPanel.repaint()

                    canGoNext = true

                } else {
                    statusPanel.status = StatusPanel.Status.ERROR
                    replaceContent(
                        getAlertComponent(
                            statusPanel,
                            statusPanel.status?.name?: "",
                            statusInfo.message
                        )
                    )
                }
            },
            EdtExecutorService.getInstance()
        ).whenCompleteAsync(
            { _, throwable ->
                if (throwable != null) {
                    statusPanel.status = StatusPanel.Status.ERROR
                    replaceContent(
                        getAlertComponent(
                            statusPanel,
                            statusPanel.status?.name?: "",
                            throwable.localizedMessage
                        )
                    )
                    statusPanel.elapsedLabel?.stop()
                }
            },
            EdtExecutorService.getInstance()
        )
    }
}