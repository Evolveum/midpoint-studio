/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.step.connection

import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.AlertPanel
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.concurrency.EdtExecutorService
import com.intellij.util.ui.JBFont
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

class BaseUrlSpecificationStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Base URL Specification"
        ) { }
    }

    override fun _init() {
        super._init()

        if (!existConnDev()) return

        if (!wizardContext.isLeavingStepByPreviousTouch) {
            submitOperation()
        }

        originalConnectorDevelopmentType = dataModel.connectorDevelopment.clone()
    }

    override fun _commit(finishChosen: Boolean) {
        dialogPanel.apply()

        dataModel.connectorDevelopment.application.baseApiEndpoint
            .requireNotBlank("REST Base Address")

        if (state == GenerateConnectorBadge.State.IN_PROGRESS ||
            state == GenerateConnectorBadge.State.EDITED
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
            state = GenerateConnectorBadge.State.COMPLETE
        }

        super._commit(finishChosen)
    }

    private fun createPanel(): JPanel = panel {
        row {
            cell(JBLabel("Set Base API URL").apply {
                font = JBFont.label().deriveFont(16f)
            })
        }

        row {
            text("""
            Enter the root URL of the target system's API or select one from suggested if they are available. This address will be used as the starting point for all communication between the connector and the external system. If the documentation provided a valid base URL, you can use the suggested value or enter one manually.
            """.trimIndent())
            .align(AlignX.FILL)
        }.bottomGap(BottomGap.MEDIUM)

        row {
            cell(AlertPanel(
                BorderLayout(), 15, "Endpoints identified",
                """
                A likely URLs for the test purposes from your documentation were detected.
                """.trimIndent()
            )).align(Align.FILL)
        }.bottomGap(BottomGap.MEDIUM)

        row("REST Base Address *") {
            textField()
                .align(AlignX.FILL)
                .bindText({ dataModel.connectorDevelopment.application.baseApiEndpoint },
                    { dataModel.connectorDevelopment.application.baseApiEndpoint = it })
                .component.apply {
                    document.addDocumentListener(object : DocumentAdapter() {
                        override fun textChanged(e: DocumentEvent) {
                            dataModel.connectorDevelopment.application.baseApiEndpoint = text
                        }
                    })
                }
        }
    }

    private fun submitOperation() {

        replaceContent(
            getLoadingComponent(
                statusPanel,
                "Identifying Connection Possibilities...",
                """
                This involves exploring and determining the available options for establishing a connection, including supported protocols, authentication methods, and endpoints.
                """.trimIndent()
            )
        )

        submitOperation(
            {
                client.submitOperationDiscoverBasicInformation(
                    dataModel.connectorDevelopment.oid
                )
            },
            { token ->
                client.getStatusInfoDiscoverBasicInformation(token)
            },
            client.project,
            "Discover Basic Information submit operation",
            true
        ).thenAcceptAsync(
            { statusInfo ->
                if (statusInfo.status.equals(OperationResultStatusType.SUCCESS)) {

                    if (isScim() == true) SCIM_BASE_URL_ITEM_NAME else BASE_ADDRESS_ITEM_NAME

                    statusInfo.result.connDevDiscoverGlobalInformationResult

                    dynamicPanel.removeAll()
                    dynamicPanel.add(createPanel())
                    dynamicPanel.revalidate()
                    dynamicPanel.repaint()

                    canGoNext = true
                } else {
                    statusPanel.status = StatusPanel.Status.ERROR
                    replaceContent(
                        getAlertComponent(
                            statusPanel,
                            statusPanel.status?.name?: "",
                            statusInfo.message ?: "Operation was not successful."
                        )
                    )
                }

                statusPanel.elapsedLabel?.stop()
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

    companion object {
        private val BASE_ADDRESS_ITEM_NAME = ItemName.from("", "baseAddress")
        private val SCIM_BASE_URL_ITEM_NAME = ItemName.from("", "scimBaseUrl")
    }
}
