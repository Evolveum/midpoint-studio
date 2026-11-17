/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.connection

import com.evolveum.midpoint.studio.client.RPCOperation
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.impl.StudioPrismContextService
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.development.util.ConnectorDevelopmentWizardUtil
import com.evolveum.midpoint.studio.ui.connector.development.util.TestingResourceState
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevHttpEndpointType
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.concurrency.EdtExecutorService
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ItemEvent
import javax.swing.*

/**
 * Endpoint for the connection test: suggested endpoints discovered by the server or a URL entered manually.
 * The chosen URL is stored on the testing resource, the test itself runs in [ResourceTestConnectorStep].
 */
class TestConnectionStep(
    wizardContext : ConnectorDevelopmentWizard,
    client : MidPointClient,
    state : Badge.State,
    isHeader : Boolean
): ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    private val globalRadioBtnGroup = ButtonGroup()
    private val endpointsListPanel = JPanel(BorderLayout())

    private var selectedEndpoint: String? = null
    private var manualSelected = false
    private val manualUrlField = JBTextField().apply {
        font = font.deriveFont(Font.PLAIN, JBUI.scaleFontSize(13f).toFloat())
        alignmentX = JPanel.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
    }

    override val nextButtonText: String
        get() = "Test Connection"

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Test Connection"
        ) {
            row {
                cell(JBLabel("Provide Endpoint for Connection Test").apply {
                    font = JBFont.label().deriveFont(16f)
                })
            }

            row {
                text("""
                Determine the best endpoint for running a test operation. This helps validate that the connector can communicate with the target system and start discovering available data structures.
                """.trimIndent()).align(AlignX.FILL)
            }.bottomGap(BottomGap.MEDIUM)

            separator()

            row {
                cell(endpointsListPanel).align(Align.FILL)
            }
        }
    }

    override fun isCompleted(resource: TestingResourceState?): Boolean =
        resource?.has(REST_TEST_ENDPOINT_PROPERTY) == true

    override fun _init() {
        super._init()

        if (!existConnDev()) return

        val suggested = dataModel.connectorDevelopment.testing?.suggestedEndpoint.orEmpty()

        if (suggested.isNotEmpty() || wizardContext.isLeavingStepByPreviousTouch) {
            showEndpoints(suggested)
        } else {
            submitOperationDiscoverConnectivityEndpoint()
        }
    }

    override fun _commit(finishChosen: Boolean) {
        val url = (if (manualSelected) manualUrlField.text else selectedEndpoint)?.trim()
        url.requireNotBlank("Endpoint URL")

        try {
            StudioPrismContextService.runWithProject(client.project) {
                ConnectorDevelopmentWizardUtil.updateTestingResourceConfiguration(
                    client,
                    dataModel.connectorDevelopment,
                    mapOf(REST_TEST_ENDPOINT_PROPERTY to url)
                )
            }
        } catch (e: Exception) {
            log.error("Couldn't save the test endpoint", e)
            throw CommitStepException("Couldn't save the test endpoint. \n Error: " + e.message)
        }

        state = Badge.State.COMPLETE

        super._commit(finishChosen)
    }

    private fun submitOperationDiscoverConnectivityEndpoint() {

        replaceContent(
            getLoadingComponent(
                statusPanel,
                "Identifying Connectivity endpoints...",
                """
                Please wait while the system is analyzing your connector configuration and discovering available connectivity endpoints.
                """.trimIndent()
            )
        )

        submitOperation(
            {
                client.submitOperationConnGenerator(
                    RPCOperation.RPC_DISCOVER_CONNECTIVITY_ENDPOINT,
                    mapOf("oid" to dataModel.connectorDevelopment.oid),
                    null
                )
            },
            { token ->
                client.getStatusInfoConnGenerator(
                    RPCOperation.RPC_DISCOVER_CONNECTIVITY_ENDPOINT,
                    token,
                )
            },
            client.project,
            "Discover Connectivity Endpoints submit operation",
            true
        ).thenAcceptAsync(
            { statusInfo ->
                statusPanel.elapsedLabel?.stop()

                if (statusInfo.status == OperationResultStatusType.SUCCESS) {
                    // the server stores the suggestions on the connector development object
                    continueFrom()

                    dynamicPanel.removeAll()
                    dynamicPanel.revalidate()
                    dynamicPanel.repaint()

                    showEndpoints(dataModel.connectorDevelopment.testing?.suggestedEndpoint.orEmpty())
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

    private fun showEndpoints(endpoints: List<ConnDevHttpEndpointType?>) {
        endpointsListPanel.removeAll()
        endpointsListPanel.add(createEndpointsListPanel(endpoints))
        endpointsListPanel.revalidate()
        endpointsListPanel.repaint()

        canGoNext = true
    }

    private fun createEndpointsListPanel(endpoints: List<ConnDevHttpEndpointType?>): JPanel = panel {

        row {
            val cardsContainer = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                isOpaque = false

                val uris = endpoints.mapNotNull { it?.uri }

                uris.forEachIndexed { index, uri ->
                    add(createCardComponent(uri, index == 0, false))
                    add(Box.createRigidArea(Dimension(0, JBUI.scale(8))))
                }

                add(createCardComponent(null, uris.isEmpty(), true))
            }

            val scrollPane = JBScrollPane(cardsContainer).apply {
                border = JBUI.Borders.empty()
                viewportBorder = JBUI.Borders.empty()
                isOpaque = false
                viewport.isOpaque = false
                verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
                horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                verticalScrollBar.unitIncrement = JBUI.scale(16)
                preferredSize = Dimension(JBUI.scale(-1), JBUI.scale(600))
            }

            cell(scrollPane).align(AlignX.FILL)
        }
    }

    private fun createCardComponent(
        endpoint: String?,
        isSelected: Boolean,
        enterUrlManually: Boolean
    ): JComponent {

        val cardRadioButton = JBRadioButton()
        globalRadioBtnGroup.add(cardRadioButton)

        val cardPanel = JPanel(BorderLayout()).apply {
            border = getUnselectedBorder()
            preferredSize = Dimension(Integer.MAX_VALUE, JBUI.scale(if (enterUrlManually) 96 else 64))
            maximumSize = Dimension(preferredSize.width, preferredSize.height)
            putClientProperty("RADIO_COMPONENT", cardRadioButton)
        }

        val verticalContainer = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        val updateState = { selected: Boolean ->
            cardRadioButton.isSelected = selected
            cardPanel.border = if (selected) getSelectedBorder() else getUnselectedBorder()

            if (selected) {
                manualSelected = enterUrlManually
                if (!enterUrlManually) {
                    selectedEndpoint = endpoint
                }
            }

            if (enterUrlManually) {
                manualUrlField.isEnabled = selected
            }

            cardPanel.revalidate()
            cardPanel.repaint()
        }

        cardRadioButton.addItemListener { event ->
            updateState(event.stateChange == ItemEvent.SELECTED)
        }

        cardPanel.add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            add(cardRadioButton)
            add(Box.createRigidArea(Dimension(JBUI.scale(12), 0)))
        }, BorderLayout.WEST)

        verticalContainer.add(Box.createRigidArea(Dimension(0, JBUI.scale(12))))
        verticalContainer.add(JBLabel(endpoint ?: ENTER_URL_MANUALLY_LABEL).apply {
            font = font.deriveFont(Font.BOLD, JBUI.scaleFontSize(13f).toFloat())
            alignmentX = JPanel.LEFT_ALIGNMENT
        })

        if (enterUrlManually) {
            verticalContainer.add(Box.createRigidArea(Dimension(0, JBUI.scale(4))))
            verticalContainer.add(manualUrlField)
        }

        cardPanel.add(verticalContainer, BorderLayout.CENTER)

        updateState(isSelected)

        return cardPanel
    }

    companion object {
        private const val ENTER_URL_MANUALLY_LABEL = "Enter URL manually"
        private const val REST_TEST_ENDPOINT_PROPERTY = "restTestEndpoint"
    }
}
