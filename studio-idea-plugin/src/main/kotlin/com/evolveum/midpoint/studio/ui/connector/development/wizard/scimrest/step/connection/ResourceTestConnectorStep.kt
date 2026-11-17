/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.connection

import com.evolveum.midpoint.schema.result.OperationResult
import com.evolveum.midpoint.studio.client.RPCOperation
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.impl.StudioPrismContextService
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.development.util.TestingResourceState
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.concurrency.EdtExecutorService
import java.util.concurrent.CompletableFuture
import javax.swing.JComponent

/**
 * Tests the testing resource and, once the connection works, refreshes its schema on the server.
 */
class ResourceTestConnectorStep(
    wizardContext : ConnectorDevelopmentWizard,
    client : MidPointClient,
    state : Badge.State,
    isHeader : Boolean
) : ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Connecting"
        ) { }
    }

    override fun isCompleted(resource: TestingResourceState?): Boolean = resource?.isUp == true

    override fun _init() {
        super._init()

        if (!existConnDev()) return

        testResource()
    }

    override fun _commit(finishChosen: Boolean) {
        state = Badge.State.COMPLETE

        super._commit(finishChosen)
    }

    private fun testResource() {
        canGoNext = false

        replaceContent(
            getLoadingComponent(
                statusPanel,
                "Testing Connection...",
                """
                Please wait while we're checking if the connector can reach the system and respond correctly.
                """.trimIndent()
            )
        )

        val oid = dataModel.connectorDevelopment.testing?.testingResource?.oid

        CompletableFuture.supplyAsync(
            {
                StudioPrismContextService.runSupplierWithProject(client.project) {
                    client.testResource(oid)
                }
            },
            ApplicationManager.getApplication()::executeOnPooledThread
        ).thenAcceptAsync(
            { result -> showTestResult(result) },
            EdtExecutorService.getInstance()
        ).whenCompleteAsync(
            { _, throwable ->
                if (throwable != null) {
                    showFailure(getExceptionMessage(throwable))
                }
            },
            EdtExecutorService.getInstance()
        )
    }

    private fun showTestResult(result: OperationResult?) {
        statusPanel.elapsedLabel?.stop()

        if (result == null) {
            showFailure("The server did not return a test result.")
            return
        }

        if (result.isSuccess || result.isWarning) {
            refreshSchema()
        } else {
            showFailure(result.message ?: result.status.toString())
        }
    }

    private fun showFailure(message: String?) {
        statusPanel.elapsedLabel?.stop()
        statusPanel.status = StatusPanel.Status.ERROR

        replaceContent(panel {
            row {
                cell(
                    getAlertComponent(
                        statusPanel,
                        "Connection Fails",
                        "The connector was unable to reach the system. " + message.orEmpty()
                    )
                ).align(Align.FILL)
            }.resizableRow()

            row {
                text("Go back to adjust the connection settings, or try again.")
                    .align(AlignX.FILL)
            }

            row {
                button("Retry") { testResource() }
            }
        })
    }

    private fun showSuccess(description: String): JComponent {
        statusPanel.status = StatusPanel.Status.SUCCESS

        return getAlertComponent(
            statusPanel,
            "Connection Successful",
            description
        )
    }

    /**
     * The GUI refreshes the schema right after a successful connection test, for REST connectors the server
     * treats it as a no-op, for SCIM and SQL it produces documentation for the generated scripts.
     */
    private fun refreshSchema() {
        replaceContent(
            getLoadingComponent(
                statusPanel,
                "Discovering schema...",
                """
                Native schema definitions are discovered from the connector and resource to be used as documentation for connector code generation.
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
                    RPCOperation.RPC_REFRESH_SCHEMA,
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
                    replaceContent(
                        showSuccess("The connector successfully reached the system and received a valid response.")
                    )
                    canGoNext = true
                } else {
                    showFailure("Schema refresh failed: " + statusInfo.message)
                }
            },
            EdtExecutorService.getInstance()
        ).whenCompleteAsync(
            { _, throwable ->
                if (throwable != null) {
                    showFailure(getExceptionMessage(throwable))
                }
            },
            EdtExecutorService.getInstance()
        )
    }
}
