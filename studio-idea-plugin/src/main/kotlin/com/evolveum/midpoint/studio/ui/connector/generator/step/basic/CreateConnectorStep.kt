package com.evolveum.midpoint.studio.ui.connector.generator.step.basic

import com.evolveum.midpoint.prism.PrismContext
import com.evolveum.midpoint.studio.client.AuthenticationException
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.impl.UploadResponse
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.util.exception.SchemaException
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import java.awt.BorderLayout
import java.io.IOException
import javax.swing.JComponent
import javax.swing.JPanel

class CreateConnectorStep (
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    dataModel : ConnectorGeneratorDataModel,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, dataModel, state, isHeader) {

    private var statusPanel = StatusPanel()
    private val mainPanel = JPanel(BorderLayout())
    private val stepComponent: DialogPanel by lazy {
        panel {
            row {
                cell(mainPanel)
                    .align(Align.FILL)
            }.resizableRow()
        }.apply {
            name = "Creating Connector"
        }
    }

    override fun _init() {

        if (getState() != GenerateConnectorBadge.State.COMPLETE) {
            setState(GenerateConnectorBadge.State.IN_PROGRESS)
        }

        canGoNext(true)

        if (getState() == GenerateConnectorBadge.State.IN_PROGRESS ||
            getState() == GenerateConnectorBadge.State.EDITED
        ) {
            ProgressManager.getInstance().run(object : Backgroundable(
                client.project,
                "Create Connector submit operation",
                true
            ) {
                private var token: String? = null

                override fun run(progressIndicator: ProgressIndicator) {
                    try {
                        statusPanel.elapsedLabel?.start()
                        token = client.submitOperationCreateConnector(
                            dataModel.connectorDevelopmentType.oid
                        )
                    } catch (e: Exception) {
                        ApplicationManager.getApplication().invokeLater {
                            printAlertPanel(
                                StatusPanel.Status.ERROR,
                                mainPanel,
                                statusPanel,
                                "Error",
                                "Failed to create connector:\n${e.message}"
                            )
                        }
                        return
                    }
                }

                override fun onSuccess() {

                    if (token == null) {
                        printTokenNullAlertPanel(mainPanel, statusPanel)
                        setState(GenerateConnectorBadge.State.FIXING)
                        return
                    }

                    printWaitingPanel(
                        mainPanel,
                        statusPanel,
                        "Creating Connector...",
                        """
                    We use the connector's basic information to create a test instance for development and testing purposes.
                    """.trimIndent()
                    )

                    getResult {
                        client.getStatusInfoCreateConnector(token)
                    }.whenComplete { statusInfoResult, ex ->
                        ApplicationManager.getApplication().invokeLater({

                            if (ex != null) {
                                printAlertPanel(
                                    StatusPanel.Status.ERROR,
                                    mainPanel,
                                    statusPanel,
                                    "Error",
                                    ex.message
                                )
                            } else {
                                val result = statusInfoResult!!.connDevCreateConnectorResult

                                if (result != null && result.getConnectorRef() != null) {
                                    try {
                                        if (createResourceObject(result.getConnectorRef()) == null) {
                                            throw Exception("Error creating resource object for connector: " + result.getConnectorRef())
                                        } else {
                                            printAlertPanel(
                                                StatusPanel.Status.SUCCESS,
                                                mainPanel,
                                                statusPanel,
                                                OperationResultStatusType.SUCCESS.name,
                                                "Successfully Generated Connector Development"
                                            )

                                            setState(GenerateConnectorBadge.State.COMPLETE)
                                            canGoNext(true)
                                            wizardContext.updateWizardButtons()
                                        }
                                    } catch (e: Exception) {
                                        printAlertPanel(
                                            StatusPanel.Status.ERROR,
                                            mainPanel,
                                            statusPanel,
                                            OperationResultStatusType.UNKNOWN.name,
                                            e.message
                                        )
                                    }
                                } else {
                                    printAlertPanel(
                                        StatusPanel.Status.ERROR,
                                        mainPanel,
                                        statusPanel,
                                        OperationResultStatusType.UNKNOWN.name,
                                        "ConnectorRef Null"
                                    )
                                }
                            }

                            statusPanel.elapsedLabel?.stop()
                        }, ModalityState.any())
                    }

                    super.onSuccess()
                }
            })
        }

        super._init()
    }

    override fun _commit(finishChosen: Boolean) {
        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent = stepComponent

    @Throws(SchemaException::class, AuthenticationException::class, IOException::class)
    private fun createResourceObject(
        objectReferenceType: ObjectReferenceType
    ): UploadResponse? {
        val prismContext: PrismContext = client.prismContext ?: return null

        val resourceObject = prismContext.createObject(ResourceType::class.java)
        val resourceType = resourceObject.asObjectable()
        resourceType.name = dataModel.connectorDevelopmentType.name
        val clonedRef: ObjectReferenceType = objectReferenceType.clone()
        resourceType.connectorRef = clonedRef

        return client.upload(resourceType.asPrismObject(), null)
    }
}