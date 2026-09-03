/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

//package com.evolveum.midpoint.studio.ui.connector.generator.step.objectclass
//
//import com.evolveum.midpoint.studio.impl.MidPointClient
//import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
//import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
//import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
//import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
//import com.intellij.openapi.application.ApplicationManager
//import com.intellij.openapi.progress.ProgressIndicator
//import com.intellij.openapi.progress.ProgressManager
//import com.intellij.openapi.progress.Task.Backgroundable
//import com.intellij.openapi.ui.DialogPanel
//import com.intellij.ui.components.JBLabel
//import com.intellij.ui.dsl.builder.Align
//import com.intellij.ui.dsl.builder.AlignX
//import com.intellij.ui.dsl.builder.BottomGap
//import com.intellij.ui.dsl.builder.panel
//import com.intellij.util.ui.JBFont
//import javax.swing.JComponent
//import javax.swing.JPanel
//
//class ObjectClassesStep(
//    wizardContext : ConnectorGeneratorWizard,
//    client : MidPointClient,
//    state : GenerateConnectorBadge.State,
//    isHeader : Boolean
//) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {
//
//    private val statusPanel = StatusPanel()
//    private val mainPanel: DialogPanel by lazy {
//        panel {
//            row {
//                cell(mainPanel)
//                    .align(Align.FILL)
//            }.resizableRow()
//        }.apply {
//            name = "Object Classes"
//        }
//    }
//
//    override fun getMainPanel(): JPanel {
//        return mainPanel
//    }
//
//    override fun getStatusPanel(): StatusPanel {
//        return statusPanel
//    }
//
//    override fun _init() {
//        super._init()
//
//        setState(GenerateConnectorBadge.State.IN_PROGRESS)
//
//        if (dataModel.connectorDevelopmentType == null) {
//            printAlertPanel(
//                StatusPanel.Status.ERROR,
//                mainPanel,
//                statusPanel,
//                "Error",
//                """
//                    Object Connector Development is null.
//                """.trimIndent()
//            )
//            return
//        }
//
//        ProgressManager.getInstance().run(object : Backgroundable(
//            client.project,
//            "Discover Object Classes submit operation",
//            true
//        ) {
//            private var token: String? = null
//
//            override fun run(progressIndicator: ProgressIndicator) {
//                try {
//                    statusPanel.elapsedLabel?.start()
//                    token = client.submitOperationDiscoverDocumentation(
//                        dataModel.connectorDevelopmentType.oid
//                    )
//                } catch (e: Exception) {
//                    ApplicationManager.getApplication().invokeLater {
//                        printAlertPanel(
//                            StatusPanel.Status.ERROR,
//                            mainPanel,
//                            statusPanel,
//                            "Error",
//                            "Failed to create connector:\n${e.message}"
//                        )
//                    }
//                    statusPanel.elapsedLabel?.stop()
//                    return
//                }
//            }
//
//            override fun onSuccess() {
//
//                if (token == null) {
//                    printTokenNullAlertPanel(mainPanel, statusPanel)
//                    setState(GenerateConnectorBadge.State.EDITED)
//                    return
//                }
//
//                printWaitingPanel(
//                    mainPanel,
//                    statusPanel,
//                    "Identifying Documentation...",
//                    """
//                    Analyzing your target application details to locate the right documentation.
//                    """)
//
////                getResult {
////                    client.getStatusInfoDiscoverDocumentation(token)
////                }.whenComplete { statusInfoResult, ex ->
////                    ApplicationManager.getApplication().invokeLater({
////                        if (ex != null) {
////                            printAlertPanel(
////                                StatusPanel.Status.ERROR,
////                                mainPanel,
////                                statusPanel,
////                                "Error",
////                                ex.message
////                            )
////                        } else {
////                            val result = statusInfoResult!!.connDevDiscoverDocumentationResult
////
////                            if (result != null && !result.documentation.isEmpty()) {
////                                mainPanel.removeAll()
////                                mainPanel.removeAll()
////                                mainPanel.add(createPanel())
////                                mainPanel.revalidate()
////                                mainPanel.repaint()
////                            } else {
////                                printAlertPanel(
////                                    StatusPanel.Status.ERROR,
////                                    mainPanel,
////                                    statusPanel,
////                                    "Error",
////                                    "No documentation found."
////                                )
////                            }
////
////                            canGoNext(true)
////                        }
////
////                        statusPanel.elapsedLabel?.stop()
////                    }, ModalityState.any())
////                }
//
//                super.onSuccess()
//            }
//        })
//    }
//
//    override fun _commit(finishChosen: Boolean) {
//        super._commit(finishChosen)
//    }
//
//    override fun getComponent(): JComponent = mainPanel
//
//    private fun createPanel(): JPanel = panel {
//
//        row {
//            cell(JBLabel("Select Object Class to Be Configured").apply {
//                font = JBFont.label().deriveFont(16f)
//            })
//        }
//
//        row {
//            text("""
//            The midPilot analyzed your documentation and found object classes that might be available for integration. Select one to begin it's configuration.
//            """.trimIndent())
//                .align(AlignX.FILL)
//        }.bottomGap(BottomGap.MEDIUM)
//
//        separator()
//    }
//
//    private fun loadingObjectClass() {
//        client.submitOperationDiscoverObjectClasses("")
//    }
//}