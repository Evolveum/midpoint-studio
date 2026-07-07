package com.evolveum.midpoint.studio.ui.connector.generator.step.connection

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.AlertPanel
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBFont
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

class BaseUrlSpecificationStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    dataModel : ConnectorGeneratorDataModel,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, dataModel, state, isHeader) {

    private var restBaseAddress = ""

    private var statusPanel = StatusPanel()
    private val mainPanel = JPanel(BorderLayout())
    private val stepComponent: DialogPanel by lazy {
        panel {
            row {
                cell(mainPanel)
                    .align(Align.FILL)
            }.resizableRow()
        }.apply {
            name = "Base URL Specification"
        }
    }

    override fun _init() {

        if (getState() != GenerateConnectorBadge.State.COMPLETE) {
            setState(GenerateConnectorBadge.State.IN_PROGRESS)
        }

        if (dataModel.connectorDevelopmentType == null) {
            printAlertPanel(
                StatusPanel.Status.ERROR,
                mainPanel,
                statusPanel,
                "Error",
                """
                    Object Connector Development is null.
                """.trimIndent()
            )
            return
        }

        if (getState() == GenerateConnectorBadge.State.IN_PROGRESS ||
            getState() == GenerateConnectorBadge.State.EDITED
        ) {
            ProgressManager.getInstance().run(object : Backgroundable(
                client.project,
                "Discover Basic Information submit operation",
                true
            ) {
                private var token: String? = null

                override fun run(progressIndicator: ProgressIndicator) {
                    try {
                        token = client.submitOperationDiscoverBasicInformation(
                            dataModel.connectorDevelopmentType.oid
                        )
                    } catch (e: Exception) {
                        ApplicationManager.getApplication().invokeLater {
                            printAlertPanel(
                                StatusPanel.Status.ERROR,
                                mainPanel,
                                statusPanel,
                                "Error",
                                "${e.message}"
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
                        "Identifying Connection Possibilities...",
                        """
                    This involves exploring and determining the available options for establishing
                    a connection, including supported protocols, authentication methods, and endpoints.
                    """)

                    getResult {
                        client.getStatusInfoDiscoverBasicInformation(token)
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
                                mainPanel.removeAll()
                                mainPanel.add(createPanel())
                                wizardContext.updateWizardButtons()
                                mainPanel.revalidate()
                                mainPanel.repaint()

                            }

                            statusPanel.elapsedLabel?.stop()

                            restBaseAddress = dataModel.connectorDevelopmentType.application.baseApiEndpoint ?: restBaseAddress

                            canGoNext(true)

                        }, ModalityState.any())
                    }

                    super.onSuccess()
                }
            })
        }

        super._init()
    }

    override fun _commit(finishChosen: Boolean) {
        stepComponent.apply()

        if (restBaseAddress.isBlank()) {
            throw CommitStepException("Field REST Base Address is required")
        }

        if (getState() == GenerateConnectorBadge.State.IN_PROGRESS ||
            getState() == GenerateConnectorBadge.State.EDITED
        ) {
            try {
                val connectorDevelopmentType: ConnectorDevelopmentType = dataModel.connectorDevelopmentType
                connectorDevelopmentType.application.baseApiEndpoint = restBaseAddress

                dataModel.connectorDevelopmentType =
                    upsertConnectorDevelopmentType(connectorDevelopmentType)
            } catch (ex: Exception) {
                throw CommitStepException("Couldn't update connector development object. \n Error: ${ex.message}")
            }
        }

        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent = stepComponent

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
                BorderLayout(), 15, "Endpoints identified", """
                A likely URLs for the test purposes from your documentation were detected.
                """.trimIndent()
            )).align(Align.FILL)
        }.bottomGap(BottomGap.MEDIUM)

        row("REST Base Address *") {
            textField()
                .align(AlignX.FILL)
                .bindText({ restBaseAddress }, { restBaseAddress = it })
                .component.apply {
                    document.addDocumentListener(object : DocumentAdapter() {
                        override fun textChanged(e: DocumentEvent) {
                            restBaseAddress = text
                        }
                    })
                }
        }
    }
}