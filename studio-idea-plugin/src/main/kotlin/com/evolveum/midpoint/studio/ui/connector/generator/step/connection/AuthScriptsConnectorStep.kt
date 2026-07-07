package com.evolveum.midpoint.studio.ui.connector.generator.step.connection

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBFont
import org.jetbrains.plugins.groovy.GroovyLanguage
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class AuthScriptsConnectorStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    dataModel : ConnectorGeneratorDataModel,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, dataModel, state, isHeader) {

    private var smartEditorComponent: SmartEditorComponent = SmartEditorComponent(client.project)

    private var statusPanel = StatusPanel()
    private val mainPanel = JPanel(BorderLayout())
    private val stepComponent: DialogPanel by lazy {
        panel {
            row {
                cell(mainPanel)
                    .align(Align.FILL)
            }.resizableRow()
        }.apply {
            name = "Auth Scripts Connector"
        }
    }

    override fun _init() {

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

        ProgressManager.getInstance().run(object : Backgroundable(
            client.project,
            "Generate Auth Script connector",
            true
        ) {
            private var token: String? = null

            override fun run(progressIndicator: ProgressIndicator) {
                try {
                    statusPanel.elapsedLabel?.start()
                    token = client.submitOperationGenerateAuthenticationScript(
                        dataModel.connectorDevelopmentType.oid,
                        false
                    )
                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        printAlertPanel(
                            StatusPanel.Status.ERROR,
                            mainPanel,
                            statusPanel,
                            "Error",
                            "Failed to generate auth script connector:\n${e.message}"
                        )
                    }
                    statusPanel.elapsedLabel?.stop()
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
                    "Generating Authentication Script...",
                    """
                    We're creating a functional authentication script based on the authentication methods supported by system. This usually takes a few seconds.
                    """)

                getResult {
                    client.getStatusInfoGenerateArtifact(token)
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
                            val result = statusInfoResult!!.connDevGenerateArtifactResult

                            if (result != null) {
                                mainPanel.removeAll()

                                smartEditorComponent.setText(
                                    result.artifact.content,
                                    GroovyLanguage
                                )

                                mainPanel.add(createPanel())
                                mainPanel.revalidate()
                                mainPanel.repaint()
                            } else {
                                printAlertPanel(
                                    StatusPanel.Status.ERROR,
                                    mainPanel,
                                    statusPanel,
                                    "Error",
                                    "No auth script found."
                                )
                            }

                            canGoNext(true)
                            wizardContext.updateWizardButtons()
                        }

                        statusPanel.elapsedLabel?.stop()
                    }, ModalityState.any())
                }

                super.onSuccess()
            }
        })

        super._init()
    }

    override fun _commit(finishChosen: Boolean) {
        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent = stepComponent

    private fun createPanel(): JPanel = panel {

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
            cell(smartEditorComponent).align(Align.FILL)
        }
    }
}