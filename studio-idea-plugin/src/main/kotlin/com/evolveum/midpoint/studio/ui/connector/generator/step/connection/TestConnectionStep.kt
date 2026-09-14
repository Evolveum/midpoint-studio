/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.step.connection

import com.evolveum.midpoint.prism.PrismObject
import com.evolveum.midpoint.prism.PrismProperty
import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.prism.path.ItemPath
import com.evolveum.midpoint.schema.constants.SchemaConstants
import com.evolveum.midpoint.studio.client.RPCOperation
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.util.exception.SchemaException
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevHttpEndpointType
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType
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
import org.jetbrains.annotations.NotNull
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.Font
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.ItemEvent
import javax.swing.*

class TestConnectionStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
): ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {

    private val globalRadioBtnGroup = ButtonGroup()
    private val endpointsListPanel = JPanel(BorderLayout())
    private var restUriEndpoint: String? = null

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

    override fun _init() {
        super._init()

        if (!wizardContext.isLeavingStepByPreviousTouch && dataModel.occurredChanges) {
            submitOperationDiscoverConnectivityEndpoint()
        }
    }

    override fun _commit(finishChosen: Boolean) {

        if (!wizardContext.isLeavingStepByPreviousTouch && dataModel.occurredChanges) {

            dataModel.connectorDevelopment.testing?.testingResource?.oid.requireNotBlank(
                "Resource oid"
            )

            try {
                restUriEndpoint.requireNotBlank(" rest URI endpoint ")

                val resource = getObjectByOid(
                    dataModel.connectorDevelopment.testing.testingResource.oid,
                    ResourceType::class.java
                )

                upsertConnectorDevelopmentType(dataModel.connectorDevelopment)

                submitOperation()
            } catch (e: Exception) {
                throw CommitStepException(e.message)
            }
        }

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

                if (statusInfo.status.equals(OperationResultStatusType.SUCCESS)) {
                    dynamicPanel.removeAll()
                    dynamicPanel.revalidate()
                    dynamicPanel.repaint()

                    endpointsListPanel.add(createEndpointsListPanelPanel(
                        dataModel.connectorDevelopment.testing.suggestedEndpoint
                    ))

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
                            throwable.localizedMessage
                        )
                    )

                    statusPanel.elapsedLabel?.stop()
                }
            },
            EdtExecutorService.getInstance()
        )
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
                if (statusInfo.status.equals(OperationResultStatusType.SUCCESS)) {
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
                            throwable.localizedMessage
                        )
                    )

                    statusPanel.elapsedLabel?.stop()
                }
            },
            EdtExecutorService.getInstance()
        )
    }

    private fun createEndpointsListPanelPanel(listHttpEndpointType: List<ConnDevHttpEndpointType?>): JPanel = panel {

        row {
            val cardsContainer = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                isOpaque = false

                var currentRestTextEndpoint: Nothing? = null
                var hasMatch = false

                listHttpEndpointType.forEach { item ->
                    if (item != null) {
                        val isSelect = item.uri.equals(currentRestTextEndpoint)
                        if (!hasMatch) hasMatch = isSelect

                        add(createCardComponent(
                            item.uri,
                            isSelect,
                            false
                        ))
                        add(Box.createRigidArea(Dimension(0, JBUI.scale(8))))
                    }
                }

                if (hasMatch) {
                    currentRestTextEndpoint = null
                }

                add(createCardComponent(
                    currentRestTextEndpoint,
                    !hasMatch,
                    true
                ))
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
            name = if (enterUrlManually) ENTER_URL_MANUALLY_CARD_ID else null
            border = getUnselectedBorder()
            preferredSize = Dimension(Integer.MAX_VALUE, JBUI.scale(64))
            maximumSize = Dimension(preferredSize.width, JBUI.scale(64))
            putClientProperty("RADIO_COMPONENT", cardRadioButton)
        }

        val verticalContainer = JPanel().apply {
            name = if (enterUrlManually) ENTER_URL_MANUALLY_CARD_ID else null
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        val updateState = { isSelected: Boolean ->
            cardRadioButton.isSelected = isSelected
            cardPanel.border = if (isSelected) getSelectedBorder() else getUnselectedBorder()

            if (isSelected) {
                if (enterUrlManually) {
                    cardPanel.maximumSize = Dimension(
                        cardPanel.preferredSize.width,
                        cardPanel.preferredSize.height + JBUI.scale(24)
                    )

                    verticalContainer.add(JBTextField(endpoint).apply {
                        name = ENTER_URL_MANUALLY_TEXT_FIELD_ID
                        font = font.deriveFont(Font.BOLD, JBUI.scaleFontSize(13f).toFloat())
                        alignmentX = JPanel.LEFT_ALIGNMENT

                        addFocusListener(object : FocusAdapter() {
                            override fun focusLost(e: FocusEvent?) {
                                restUriEndpoint = text
                            }
                        })
                    })
                } else {

                    val parent = cardPanel.parent

                    if (parent != null) {
                        parent.components.forEach {card ->
                            if (card.name == ENTER_URL_MANUALLY_CARD_ID) {
                                (card as Container).components.forEach {verticalContainer ->
                                    (verticalContainer as Container).components.forEach { field ->
                                        if (field.name == ENTER_URL_MANUALLY_TEXT_FIELD_ID) {
                                            verticalContainer.remove(field)
                                            card.maximumSize = Dimension(
                                                card.preferredSize.width,
                                                card.maximumSize.height - JBUI.scale(24)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    restUriEndpoint = endpoint
                }
            }

            cardPanel.revalidate()
            cardPanel.repaint()
        }

        cardRadioButton.addItemListener { event ->
            val isSelected = event.stateChange == ItemEvent.SELECTED
            updateState(isSelected)
        }

        cardPanel.add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            add(cardRadioButton)
            add(Box.createRigidArea(Dimension(JBUI.scale(12), 0)))
        }, BorderLayout.WEST)

        if (enterUrlManually) {
            verticalContainer.add(Box.createRigidArea(Dimension(JBUI.scale(0), 12)))
            verticalContainer.add(JBLabel(ENTER_URL_MANUALLY_LABEL).apply {
                font = font.deriveFont(Font.BOLD, JBUI.scaleFontSize(13f).toFloat())
                alignmentX = JPanel.LEFT_ALIGNMENT
                alignmentY = JPanel.CENTER_ALIGNMENT
            })

            if (isSelected) {
                cardPanel.maximumSize = Dimension(
                    cardPanel.preferredSize.width,
                    cardPanel.preferredSize.height + JBUI.scale(24)
                )

                verticalContainer.add(JBTextField(endpoint).apply {
                    name = ENTER_URL_MANUALLY_TEXT_FIELD_ID
                    font = font.deriveFont(Font.PLAIN, JBUI.scaleFontSize(13f).toFloat())
                    alignmentX = JPanel.LEFT_ALIGNMENT

                    maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
                })
            }
        } else {
            endpoint?.let { endpointText ->
                verticalContainer.add(Box.createRigidArea(Dimension(JBUI.scale(0), 12)))
                verticalContainer.add(JBLabel(endpointText).apply {
                    font = font.deriveFont(Font.BOLD, JBUI.scaleFontSize(13f).toFloat())
                    alignmentX = JPanel.LEFT_ALIGNMENT
                    alignmentY = JPanel.CENTER_ALIGNMENT
                })
            }
        }

        cardPanel.add(verticalContainer,BorderLayout.CENTER)

        updateState(isSelected)

        return cardPanel
    }

    @Throws(SchemaException::class)
    private fun setRestTestEndpoint(
        @NotNull resource: PrismObject<ResourceType>,
        url: String?
    ) {
//
//        try {
//            val container: PrismContainer<Containerable> = resource.findOrCreateContainer(
//                ItemPath.create(
//                    "connectorConfiguration",
//                    SchemaConstants.ICF_CONFIGURATION_PROPERTIES_LOCAL_NAME
//                )
//            )
//
//            container.definition?.definitions?.forEach {
//                log.info("Defined configuration property: ${it.itemName}")
//            }
//
////            container.findOrCreateProperty<String>(
////                ItemPath.create(PROPERTY_ITEM_NAME)
////            ).realValue = url
//        } catch (e: Exception) {
//            log.error(e)
//            throw e
//        }
    }

    @Throws(SchemaException::class)
    private fun updateResource(
        @NotNull resource: PrismObject<ResourceType>
    ): ResourceType? {
        return client.upsert(resource, null)
    }

    private fun resourceTesting(@NotNull oid: String) {

        replaceContent(
            getLoadingComponent(
                statusPanel,
                "Testing Connection...",
                """
                Please wait while we're checking if the connector can reach the system and respond correctly.
                """.trimIndent()
            )
        )

        client.testResource(oid)
    }

    companion object {
        private const val ENTER_URL_MANUALLY_CARD_ID = "urlManuallyCard"
        private const val ENTER_URL_MANUALLY_TEXT_FIELD_ID = "urlManuallyTextField"
        private const val ENTER_URL_MANUALLY_LABEL = "Enter URL manually"
        private val PROPERTY_ITEM_NAME = ItemName.from("", "restTestEndpoint")
    }
}