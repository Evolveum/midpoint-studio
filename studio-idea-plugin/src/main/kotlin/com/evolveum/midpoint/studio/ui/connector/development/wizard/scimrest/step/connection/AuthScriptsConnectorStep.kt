/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.connection

import com.evolveum.midpoint.studio.client.RPCOperation
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.development.util.TestingResourceState
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.studio.ui.converter.LanguageSelectorPanel
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent
import com.evolveum.midpoint.studio.util.LanguageUtils
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBOptionButton
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.concurrency.EdtExecutorService
import com.intellij.util.ui.JBFont
import org.jetbrains.plugins.groovy.GroovyLanguage
import org.jetbrains.yaml.YAMLLanguage
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JPanel


class AuthScriptsConnectorStep(
    wizardContext : ConnectorDevelopmentWizard,
    client : MidPointClient,
    state : Badge.State,
    isHeader : Boolean
) : ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    private var scriptEditor: SmartEditorComponent? = null

    private val confirmAction = object : AbstractAction("Yes, this script is correct") {
        override fun actionPerformed(e: ActionEvent) = wizardContext.proceedToNextStep()
    }

    private val discardSuggestedAction = object : AbstractAction("Discard suggested script") {
        override fun actionPerformed(e: ActionEvent) = discardSuggestedScript()
    }

    private val regenerateAction = object : AbstractAction("Regenerate") {
        override fun actionPerformed(e: ActionEvent) = submitOperation(retry = true)
    }

    // midPoint 4.11.0 REST has no operation to delete a deployed artifact, enable once the client has it
    private val discardScriptAction = object : AbstractAction("Discard script") {
        override fun actionPerformed(e: ActionEvent) {}
    }.apply { isEnabled = false }

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Authentication script validation"
        ) { }
    }

    /**
     * Studio cannot save the script yet (no server endpoint), so a working connection counts as done here, same as
     * a deployed script does in the web GUI.
     *
     * TODO improve when server api is in place
     */
    override fun isCompleted(resource: TestingResourceState?): Boolean =
        resource?.isUp == true || dataModel.connectorDevelopment.connector?.authenticationScript != null

    override fun _init() {
        super._init()

        if (!wizardContext.isLeavingStepByPreviousTouch) {
            submitOperation(retry = false)
        }
    }

    // the step has its own split button with the secondary actions, the wizard Next button is hidden
    override val nextButtonText: String?
        get() = null

    override fun _commit(finishChosen: Boolean) {

        if (state == Badge.State.IN_PROGRESS ||
            state == Badge.State.EDITED
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
            updateConnectorDevelopment()
            state = Badge.State.COMPLETE
        }

        super._commit(finishChosen)
    }

    fun createPanel(content: String): JPanel = panel {
        val smartEditorComponent = SmartEditorComponent(client.project)
        scriptEditor = smartEditorComponent

        smartEditorComponent.isViewer = false

        // generated content is only a suggestion until the step is confirmed, same as in the web GUI
        discardSuggestedAction.isEnabled = content.isNotBlank()

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
            cell(
                JBOptionButton(
                    confirmAction,
                    arrayOf(discardSuggestedAction, regenerateAction, discardScriptAction)
                )
            ).align(AlignX.RIGHT)
        }
    }

    /**
     * Drops the generated suggestion: shows the deployed script if its content is known, otherwise clears the editor.
     */
    private fun discardSuggestedScript() {
        val deployed = dataModel.connectorDevelopment.connector?.authenticationScript
            ?.takeIf { it.isDisabled != true }
            ?.content
            .orEmpty()

        scriptEditor?.text = deployed
        discardSuggestedAction.isEnabled = false
    }

    private fun submitOperation(retry: Boolean) {

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
                        "retry" to retry
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

                    val artifact = statusInfo.result?.connDevGenerateArtifactResult?.artifact?: run {
                        statusPanel.status = StatusPanel.Status.ERROR
                        replaceContent(
                            getAlertComponent(
                                statusPanel,
                                statusPanel.status?.name?: "",
                                "Failed to generate the artifact result",
                            )
                        )

                        return@thenAcceptAsync
                    }

                    val content = artifact.content?: run {
                        statusPanel.status = StatusPanel.Status.ERROR
                        replaceContent(
                            getAlertComponent(
                                statusPanel,
                                statusPanel.status?.name?: "",
                                "Failed to generate the script",
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