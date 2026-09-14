/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.step.basic

import com.amazon.ion.NullValueException
import com.evolveum.midpoint.prism.PrismConstants
import com.evolveum.midpoint.prism.PrismContext
import com.evolveum.midpoint.studio.client.AuthenticationException
import com.evolveum.midpoint.studio.client.RPCOperation
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.impl.UploadResponse
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.util.exception.SchemaException
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType
import com.intellij.openapi.ui.DialogPanel
import com.intellij.util.concurrency.EdtExecutorService
import java.io.IOException

class CreateConnectorStep (
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Creating Connector"
        ) { }
    }

    override val nextButtonText: String
        get() = "Download Connector"

    override fun _init() {
        super._init()

        if (!existConnDev()) return

        if (!wizardContext.isLeavingStepByPreviousTouch && dataModel.occurredChanges) {
            submitOperation()
        }
    }

    private fun submitOperation() {
        replaceContent(
            getLoadingComponent(
                statusPanel,
                "Creating Connector...",
                """
                We use the connector's basic information to create a test instance for development and testing purposes.
                """
            )
        )

        submitOperation(
            {
                client.submitOperationConnGenerator(
                    RPCOperation.RPC_CREATE_CONNECTOR,
                    mapOf("oid" to dataModel.connectorDevelopment.oid),
                    null
                )
            },
            { token ->
                client.getStatusInfoConnGenerator(
                    RPCOperation.RPC_CREATE_CONNECTOR,
                    token
                )
            },
            client.project,
            "Create Connector submit operation",
            true
        ).thenAcceptAsync(
            { statusInfo ->
                if (statusInfo.status == OperationResultStatusType.SUCCESS) {
                    try {
                        val result = statusInfo.result.connDevCreateConnectorResult
                        val connectorRef = result?.connectorRef ?: run {
                            throw NullValueException("ConnectorRef Null")
                        }

                        val testingResource = createTestingResource(connectorRef) ?: run {
                            throw NullValueException("Failed to create testing resource")
                        }

                        val resourceRef = ObjectReferenceType()
                        resourceRef.oid = testingResource.oid
                        resourceRef.type = ResourceType.COMPLEX_TYPE
                        resourceRef.relation = PrismConstants.Q_ANY

                        dataModel.connectorDevelopment.testing.testingResource = resourceRef
                        dataModel.connectorDevelopment.connector.connectorRef = connectorRef

                        upsertConnectorDevelopmentType(dataModel.connectorDevelopment)

                        statusPanel.status = StatusPanel.Status.SUCCESS
                        replaceContent(
                            getAlertComponent(
                                statusPanel,
                                statusInfo.status.name,
                                "Successfully Generated Connector Development"
                            )
                        )

                        state = GenerateConnectorBadge.State.COMPLETE
                        canGoNext = true
                    } catch (e: Exception) {
                        statusPanel.status = StatusPanel.Status.ERROR
                        replaceContent(
                            getAlertComponent(
                                statusPanel,
                                statusPanel.status?.name ?: "",
                                e.message
                            )
                        )
                    }
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
                            statusPanel.status?.name ?: "",
                            throwable.localizedMessage
                        )
                    )

                    statusPanel.elapsedLabel?.stop()
                }
            },
            EdtExecutorService.getInstance()
        )
    }

    @Throws(SchemaException::class, AuthenticationException::class, IOException::class)
    private fun createTestingResource(
        connectorRef: ObjectReferenceType
    ): ResourceType? {
        val prismContext: PrismContext = client.prismContext ?: return null
        val resourceObject = prismContext.createObject(ResourceType::class.java)
        val resourceType = resourceObject.asObjectable()
        resourceType.name = PolyStringType("Resource - " + dataModel.connectorDevelopment.name?.orig)
        resourceType.connectorRef = connectorRef.clone()

        return client.upsert(resourceType.asPrismObject(), null)
    }
}