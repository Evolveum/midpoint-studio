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
import com.evolveum.midpoint.studio.ui.converter.LanguageSelectorPanel
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent
import com.evolveum.midpoint.studio.util.LanguageUtils
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.concurrency.EdtExecutorService
import com.intellij.util.ui.JBFont
import org.jetbrains.plugins.groovy.GroovyLanguage
import org.jetbrains.yaml.YAMLLanguage
import javax.swing.JPanel


class AuthScriptsConnectorStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Authentication script validation"
        ) { }
    }

    override fun _init() {
        super._init()

        if (!wizardContext.isLeavingStepByPreviousTouch) {
            submitOperation()
        }
    }

    override val nextButtonText: String
        get() = "Yes, this script is correct"

    override fun _commit(finishChosen: Boolean) {

        if (state == GenerateConnectorBadge.State.IN_PROGRESS ||
            state == GenerateConnectorBadge.State.EDITED
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
            updateConnectorDevelopment()
            state = GenerateConnectorBadge.State.COMPLETE
        }

        super._commit(finishChosen)
    }

    fun createPanel(content: String): JPanel = panel {
        val smartEditorComponent = SmartEditorComponent(client.project)

        smartEditorComponent.isViewer = false

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

        row {
            val targetLangSelector = LanguageSelectorPanel(
                "Target language",
                arrayOf(
                    YAMLLanguage.INSTANCE.id,
                    GroovyLanguage.id
                )
            )

            targetLangSelector.onLanguageChange {
                smartEditorComponent.switchLanguage(
                    LanguageUtils.findLanguageByID(targetLangSelector.selectedLanguage)
                )
            }

            cell(targetLangSelector).align(AlignX.RIGHT)
        }

        row {
            smartEditorComponent.setText(content, GroovyLanguage)
            cell(smartEditorComponent).align(Align.FILL)
        }

        row {
            comboBox(
                listOf(
                    "Discard suggested script",
                    "Regenerated",
                    "Discard script"
                )
            ).applyToComponent { selectedItem = null }
            .align(AlignX.RIGHT)
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