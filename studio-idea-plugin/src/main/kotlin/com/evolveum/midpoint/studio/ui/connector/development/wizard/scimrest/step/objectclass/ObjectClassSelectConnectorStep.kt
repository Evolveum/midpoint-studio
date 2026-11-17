/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass

import com.evolveum.midpoint.studio.client.RPCOperation
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.development.util.TestingResourceState
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevObjectClassInfoType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevRelevancyLevelType
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
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ItemEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

/**
 * Object class to configure: one of the classes the server detected in the documentation, or a name entered by
 * hand. Detection runs on the first visit, the chosen class is added to the connector on the server.
 */
class ObjectClassSelectConnectorStep(
    wizardContext : ConnectorDevelopmentWizard,
    client: MidPointClient,
    state : Badge.State,
    isHeader: Boolean
): ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    private val globalRadioBtnGroup = ButtonGroup()
    private val candidatesPanel = JPanel(BorderLayout())

    private var selectedCandidate: ConnDevObjectClassInfoType? = null
    private var manualSelected = false
    private val manualNameField = JBTextField().apply {
        font = font.deriveFont(Font.PLAIN, JBUI.scaleFontSize(13f).toFloat())
        alignmentX = JPanel.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
    }

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Object Classes"
        ) {
            row {
                cell(JBLabel("Select Object Class to Be Configured").apply {
                    font = JBFont.label().deriveFont(16f)
                })
            }

            row {
                text("""
                The midPilot analyzed your documentation and found object classes that might be available for integration. Select one to begin its configuration, or enter a name to create a new object class with a basic skeleton configuration.
                """.trimIndent()).align(AlignX.FILL)
            }.bottomGap(BottomGap.MEDIUM)

            separator()

            row {
                cell(candidatesPanel).align(Align.FILL)
            }
        }
    }

    override fun isCompleted(resource: TestingResourceState?): Boolean = configuredObjectClasses().isNotEmpty()

    override fun _init() {
        super._init()

        if (!existConnDev()) return

        originalConnectorDevelopmentType = dataModel.connectorDevelopment.clone()

        if (detectedObjectClasses().isNotEmpty() || wizardContext.isLeavingStepByPreviousTouch) {
            showCandidates()
        } else {
            submitOperationDiscoverObjectClasses()
        }
    }

    override fun _commit(finishChosen: Boolean) {
        val objectClass = if (manualSelected) {
            val name = manualNameField.text.trim()
            name.requireNotBlank("Object class name")
            ConnDevObjectClassInfoType().name(name) as ConnDevObjectClassInfoType
        } else {
            val candidate = selectedCandidate
                ?: throw CommitStepException("Select an object class.")
            // the detected value carries the description, relevancy and discovered endpoints and attributes
            candidate.clone().also { it.asPrismContainerValue().id = null }
        }

        val duplicate = configuredObjectClasses().any { it.name.equals(objectClass.name, ignoreCase = true) }
        if (duplicate) {
            throw CommitStepException(
                "An object class with this name already exists. Please choose a different name.")
        }

        dataModel.connectorDevelopment.connector.objectClass.add(objectClass)

        dataModel.occurredChanges = true
        updateConnectorDevelopment()
        state = Badge.State.COMPLETE

        super._commit(finishChosen)
    }

    private fun detectedObjectClasses(): List<ConnDevObjectClassInfoType> =
        dataModel.connectorDevelopment.application?.detectedSchema?.objectClass.orEmpty()

    private fun configuredObjectClasses(): List<ConnDevObjectClassInfoType> =
        dataModel.connectorDevelopment.connector?.objectClass.orEmpty()

    /**
     * Detected classes not configured yet, most relevant first, same as the recommended list in the web GUI.
     */
    private fun candidates(): List<ConnDevObjectClassInfoType> {
        val configured = configuredObjectClasses().map { it.name.lowercase() }.toSet()

        return detectedObjectClasses()
            .filter { it.name != null && it.name.lowercase() !in configured }
            .sortedByDescending { it.relevancy?.ordinal ?: -1 }
    }

    private fun submitOperationDiscoverObjectClasses() {

        replaceContent(
            getLoadingComponent(
                statusPanel,
                "Identifying Object Classes...",
                """
                Analyzing documentation to determine which object classes are supported by the connected system.
                """.trimIndent()
            )
        )

        submitOperation(
            {
                client.submitOperationConnGenerator(
                    RPCOperation.RPC_DISCOVER_OBJECT_CLASSES,
                    mapOf("oid" to dataModel.connectorDevelopment.oid),
                    null
                )
            },
            { token ->
                client.getStatusInfoConnGenerator(
                    RPCOperation.RPC_DISCOVER_OBJECT_CLASSES,
                    token
                )
            },
            client.project,
            "Discover Object Classes submit operation",
            true
        ).thenAcceptAsync(
            { statusInfo ->
                statusPanel.elapsedLabel?.stop()

                if (statusInfo.status == OperationResultStatusType.SUCCESS) {
                    // the server stores the detected schema on the connector development object
                    continueFrom()
                    originalConnectorDevelopmentType = dataModel.connectorDevelopment.clone()

                    dynamicPanel.removeAll()
                    dynamicPanel.revalidate()
                    dynamicPanel.repaint()

                    showCandidates()
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

    private fun showCandidates() {
        candidatesPanel.removeAll()
        candidatesPanel.add(createCandidatesListPanel(candidates()))
        candidatesPanel.revalidate()
        candidatesPanel.repaint()

        canGoNext = true
    }

    private fun createCandidatesListPanel(candidates: List<ConnDevObjectClassInfoType>): JPanel = panel {

        row {
            val cardsContainer = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                isOpaque = false

                candidates.forEachIndexed { index, candidate ->
                    add(createCardComponent(candidate, index == 0))
                    add(Box.createRigidArea(Dimension(0, JBUI.scale(8))))
                }

                add(createManualCardComponent(candidates.isEmpty()))
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

    private fun createCardComponent(candidate: ConnDevObjectClassInfoType, isSelected: Boolean): JComponent {
        val title = buildString {
            append(candidate.name)
            if (candidate.relevancy == ConnDevRelevancyLevelType.HIGH) {
                append("  (recommended)")
            }
        }

        return createCard(title, candidate.description, 80, isSelected) { selected ->
            if (selected) {
                manualSelected = false
                selectedCandidate = candidate
            }
        }
    }

    private fun createManualCardComponent(isSelected: Boolean): JComponent =
        createCard(ENTER_NAME_MANUALLY_LABEL, null, 96, isSelected) { selected ->
            if (selected) {
                manualSelected = true
            }
            manualNameField.isEnabled = selected
        }

    private fun createCard(
        title: String,
        description: String?,
        height: Int,
        isSelected: Boolean,
        onSelectionChanged: (Boolean) -> Unit
    ): JComponent {

        val cardRadioButton = JBRadioButton()
        globalRadioBtnGroup.add(cardRadioButton)

        val cardPanel = JPanel(BorderLayout()).apply {
            border = getUnselectedBorder()
            preferredSize = Dimension(Integer.MAX_VALUE, JBUI.scale(height))
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
            onSelectionChanged(selected)
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
        verticalContainer.add(JBLabel(title).apply {
            font = font.deriveFont(Font.BOLD, JBUI.scaleFontSize(13f).toFloat())
            alignmentX = JPanel.LEFT_ALIGNMENT
        })

        if (!description.isNullOrBlank()) {
            verticalContainer.add(Box.createRigidArea(Dimension(0, JBUI.scale(4))))
            verticalContainer.add(JBLabel(description).apply {
                foreground = UIUtil.getContextHelpForeground()
                alignmentX = JPanel.LEFT_ALIGNMENT
            })
        }

        if (title == ENTER_NAME_MANUALLY_LABEL) {
            verticalContainer.add(Box.createRigidArea(Dimension(0, JBUI.scale(4))))
            verticalContainer.add(manualNameField)
        }

        cardPanel.add(verticalContainer, BorderLayout.CENTER)

        updateState(isSelected)

        return cardPanel
    }

    companion object {
        private const val ENTER_NAME_MANUALLY_LABEL = "Enter object class name manually"
    }
}
