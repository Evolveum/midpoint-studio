/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.connection

import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.studio.client.RPCOperation
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.evolveum.midpoint.studio.ui.connector.development.component.AlertPanel
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.concurrency.EdtExecutorService
import com.intellij.util.ui.JBFont
import java.awt.BorderLayout

class BaseUrlSpecificationStep(
    wizardContext : ConnectorDevelopmentWizard,
    client : MidPointClient,
    state : Badge.State,
    isHeader : Boolean
) : ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Identifying"
        ) { }
    }

    // panel with the bound field lives inside dynamicPanel, so it has to be applied separately
    private var contentPanel: DialogPanel? = null

    override fun _init() {
        super._init()

        if (!existConnDev()) return

        if (!wizardContext.isLeavingStepByPreviousTouch) {
            submitOperation()
        }

        originalConnectorDevelopmentType = dataModel.connectorDevelopment.clone()
    }

    override fun _commit(finishChosen: Boolean) {
        contentPanel?.apply()

        dataModel.connectorDevelopment.application.baseApiEndpoint
            .requireNotBlank("REST Base Address")

        if (state == Badge.State.IN_PROGRESS ||
            state == Badge.State.EDITED
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)

            updateConnectorDevelopment()
            state = Badge.State.COMPLETE
        }

        super._commit(finishChosen)
    }

    private fun createPanel(): DialogPanel = panel {

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

        if (!dataModel.connectorDevelopment.application?.baseApiEndpoint.isNullOrBlank()) {
            row {
                cell(AlertPanel(
                    BorderLayout(), 15, "Endpoints identified",
                    """
                    A likely URLs for the test purposes from your documentation were detected.
                    """.trimIndent()
                )).align(Align.FILL)
            }.bottomGap(BottomGap.MEDIUM)
        }

        row("REST Base Address *") {
            textField()
                .align(AlignX.FILL)
                .bindText(
                    { dataModel.connectorDevelopment.application?.baseApiEndpoint.orEmpty() },
                    { value ->
                        dataModel.connectorDevelopment.application.baseApiEndpoint =
                            value.takeIf { it.isNotBlank() }
                    }
                )
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
                client.submitOperationConnGenerator(
                    RPCOperation.RPC_DISCOVER_BASIC_INFORMATION,
                    mapOf("oid" to dataModel.connectorDevelopment.oid),
                    null
                )
            },
            { token ->
                client.getStatusInfoConnGenerator(
                    RPCOperation.RPC_DISCOVER_BASIC_INFORMATION,
                    token
                )
            },
            client.project,
            "Discover Basic Information submit operation",
            true
        ).thenAcceptAsync(
            { statusInfo ->
                if (statusInfo.status == OperationResultStatusType.SUCCESS) {

                    // discovery writes into the server-side object; reload it before rendering
                    continueFrom()

                    contentPanel = createPanel()

                    dynamicPanel.removeAll()
                    dynamicPanel.add(contentPanel)
                    dynamicPanel.revalidate()
                    dynamicPanel.repaint()

                    dialogPanel.name = "Base URL Specification"
                    wizardContext.updateNavigationMenuByLiveStates()

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
                            getExceptionMessage(throwable)
                        )
                    )
                    statusPanel.elapsedLabel?.stop()
                }
            },
            EdtExecutorService.getInstance()
        )
    }

    companion object {
        val BASE_ADDRESS_ITEM_NAME: ItemName.WithoutPrefix = ItemName.from("", "baseAddress")
        val SCIM_BASE_URL_ITEM_NAME: ItemName.WithoutPrefix = ItemName.from("", "scimBaseUrl")
    }
}
