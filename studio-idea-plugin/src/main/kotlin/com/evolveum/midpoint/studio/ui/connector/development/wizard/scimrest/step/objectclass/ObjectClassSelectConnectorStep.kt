/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass

import com.evolveum.midpoint.studio.client.RPCOperation
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.intellij.openapi.ui.DialogPanel
import com.intellij.util.concurrency.EdtExecutorService

class ObjectClassSelectConnectorStep(
    wizardContext : ConnectorDevelopmentWizard,
    client: MidPointClient,
    state : Badge.State,
    isHeader: Boolean
): ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Object Classes"
        ) { }
    }

    override fun _init() {
        super._init()
    }

    override fun _commit(finishChosen: Boolean) {
        super._commit(finishChosen)
    }


    private fun submitOperation() {

        replaceContent(
            getLoadingComponent(
                statusPanel,
                "Discovering schema...",
                """
                Object classes and their attributes are extracted from the shadows and saved as processed documentation for connector code generation.
                """.trimIndent()
            )
        )

        submitOperation(
            {
                client.submitOperationConnGenerator(
                    RPCOperation.RPC_REFRESH_SCHEMA,
                    mapOf("oid" to dataModel.connectorDevelopment.oid),
                    null
                )
            },
            { token ->
                client.getStatusInfoConnGenerator(
                    RPCOperation.RPC_DISCOVER_CONNECTIVITY_ENDPOINT,
                    token
                )
            },
            client.project,
            "Refresh Schema submit operation",
            true
        ).thenAcceptAsync(
            { statusInfo ->
                statusPanel.elapsedLabel?.stop()
                if (statusInfo.status == OperationResultStatusType.SUCCESS) {
                    canGoNext = true
                } else {
                    statusPanel.status = StatusPanel.Status.ERROR
                    replaceContent(
                        getAlertComponent(
                            statusPanel,
                            statusPanel.status?.name ?: "",
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
                            statusPanel.status?.name ?: "",
                            getExceptionMessage(throwable)
                        )
                    )

                    statusPanel.elapsedLabel?.stop()
                }
            },
            EdtExecutorService.getInstance()
        )
    }

}

