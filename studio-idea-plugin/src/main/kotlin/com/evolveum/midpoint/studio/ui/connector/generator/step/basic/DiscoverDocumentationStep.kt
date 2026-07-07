package com.evolveum.midpoint.studio.ui.connector.generator.step.basic

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.AlertPanel
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDocumentationSourceType
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.panels.VerticalLayout
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class DiscoverDocumentationStep(
    wizardContext: ConnectorGeneratorWizard,
    client: MidPointClient,
    dataModel: ConnectorGeneratorDataModel,
    state: GenerateConnectorBadge.State,
    isHeader: Boolean
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
            name = "Documentation"
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
                "Discover Documentation submit operation",
                true
            ) {
                private var token: String? = null

                override fun run(progressIndicator: ProgressIndicator) {
                    try {
                        statusPanel.elapsedLabel?.start()
                        token = client.submitOperationDiscoverDocumentation(
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
                        "Identifying Documentation...",
                        """
                    Analyzing your target application details to locate the right documentation.
                    """)

                    getResult {
                        client.getStatusInfoDiscoverDocumentation(token)
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
                                val result = statusInfoResult!!.connDevDiscoverDocumentationResult

                                if (result != null && !result.documentation.isEmpty()) {
                                    mainPanel.removeAll()
                                    cardsContainer.removeAll()
                                    mainPanel.add(createPanel(result.documentation))
                                    mainPanel.revalidate()
                                    mainPanel.repaint()
                                } else {
                                    printAlertPanel(
                                        StatusPanel.Status.ERROR,
                                        mainPanel,
                                        statusPanel,
                                        "Error",
                                        "No documentation found."
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
        }

        super._init()
    }

    @Throws(CommitStepException::class)
    override fun _commit(finishChosen: Boolean) {

        if (getState() == GenerateConnectorBadge.State.IN_PROGRESS ||
            getState() == GenerateConnectorBadge.State.EDITED
        ) {
            try {
                dataModel.connectorDevelopmentType =
                    upsertConnectorDevelopmentType(dataModel.connectorDevelopmentType)
            } catch (ex: Exception) {
                throw CommitStepException("Couldn't update connector development object. \n Error: " + ex.message)
            }

            setState(GenerateConnectorBadge.State.COMPLETE)
        }

        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent = stepComponent

    fun createPanel(documentations : List<ConnDevDocumentationSourceType>): JPanel = panel {

        row {
            cell(JBLabel("Identify the target application").apply {
                font = JBFont.label().deriveFont(16f)
            })
        }

        row {
            text("Tell us which application you want to connect to. Based on this information, the system will identify the target and locate appropriate documentation.")
                .align(AlignX.FILL)
        }.bottomGap(BottomGap.MEDIUM)

        row {
            cell(AlertPanel(
                BorderLayout(), 15, "Documentation found", """
                AI has found matching documentation or configurations. Please review them to ensure they fit your needs..
                """.trimIndent()
            )).align(Align.FILL)
        }.bottomGap(BottomGap.MEDIUM)

        row {
            checkBox("All").applyToComponent {
                addItemListener { event ->
                    toggleAllCardsSelection(event.stateChange == ItemEvent.SELECTED)
                }
            }.align(AlignX.LEFT)

            val actionsWrapper = JPanel(FlowLayout(FlowLayout.RIGHT, JBUI.scale(8), 0)).apply {
                isOpaque = false

                add(JButton("+ Add URL").apply {
                    addActionListener {
                        // TODO: impl action
                    }
                })

                add(JButton("Upload File").apply {
                    addActionListener {
                        // TODO: impl action
                    }
                })
            }

            cell(actionsWrapper).align(AlignX.RIGHT)
        }

        row {
            cell(scrollPane)
                .align(AlignX.FILL)
        }

        documentations.forEach { data ->
            cardsContainer.add(createCardComponent(data))
        }
    }

    private fun toggleAllCardsSelection(shouldSelectAll: Boolean) {
        for (component in cardsContainer.components) {
            if (component is JPanel) {
                val leftPanel = component.components.firstOrNull {
                    it is JPanel && it.layout is BoxLayout
                } as? JPanel

                val checkBox = leftPanel?.components?.firstOrNull { it is JCheckBox } as? JCheckBox
                checkBox?.isSelected = shouldSelectAll
            }
        }
        cardsContainer.revalidate()
        cardsContainer.repaint()
    }

    private val cardsContainer = JPanel(VerticalLayout(JBUI.scale(10))).apply {
        background = UIUtil.getPanelBackground()
        border = JBUI.Borders.empty(10)
    }

    private val scrollPane = JBScrollPane(cardsContainer).apply {
        border = JBUI.Borders.empty()
        viewportBorder = JBUI.Borders.empty()
        preferredSize = Dimension(JBUI.scale(-1), JBUI.scale(600))
    }

    private fun createCardComponent(documentationSource: ConnDevDocumentationSourceType): JComponent {

        val cardCheckbox = JCheckBox()

        val cardPanel = JPanel(BorderLayout()).apply {
            border = getUnselectedBorder()
        }

        val updateState = { isSelected: Boolean ->
            cardCheckbox.isSelected = isSelected
            cardPanel.border = if (isSelected) getSelectedBorder() else getUnselectedBorder()
            cardPanel.revalidate()
            cardPanel.repaint()
        }

        if (dataModel.connectorDevelopmentType.documentationSource.contains(documentationSource)) {
            updateState(true)
        }

        cardCheckbox.addItemListener { event ->
            val isSelected = event.stateChange == ItemEvent.SELECTED

            if (isSelected) {
                dataModel.connectorDevelopmentType.documentationSource.add(documentationSource.clone())
            } else {
                dataModel.connectorDevelopmentType.documentationSource.remove(documentationSource.clone())
            }

            updateState(isSelected)
        }

        cardPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.source != cardCheckbox) {
                    cardCheckbox.isSelected = !cardCheckbox.isSelected
                }
            }
        })

        val leftActionsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            add(cardCheckbox)
            add(Box.createRigidArea(Dimension(JBUI.scale(8), 0)))
            add(JBLabel(AllIcons.Nodes.Plugin).apply { foreground = JBColor.GRAY })
            add(Box.createRigidArea(Dimension(JBUI.scale(12), 0)))
        }
        cardPanel.add(leftActionsPanel, BorderLayout.WEST)

        val textLayoutContentBody = JPanel(VerticalLayout(JBUI.scale(4))).apply {
            isOpaque = false

            val headerLineWrapper = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                isOpaque = false

                val titleLabel = JBLabel(documentationSource.name).apply {
                    font = font.deriveFont(Font.BOLD, JBUI.scaleFontSize(13f).toFloat())
                }
                add(titleLabel)

                // use AI tag
                if (true) {
                    add(Box.createRigidArea(Dimension(JBUI.scale(8), 0)))
                    add(GenerateConnectorBadge(GenerateConnectorBadge.AiTag.AI_TAG))
                }
            }
            add(headerLineWrapper)

            val descriptionLabel = JTextArea(documentationSource.description).apply {
                font = font.deriveFont(JBUI.scaleFontSize(11.5f).toFloat())
                foreground = JBColor(0x666666, 0xBBBBBB)
                lineWrap = true
                wrapStyleWord = true
                isEditable = false
                isOpaque = false
                border = null
                isFocusable = false
            }
            add(descriptionLabel)

            val uriLink = LinkLabel<Any?>(
                documentationSource.uri,
                null
            ) { _: LinkLabel<Any?>?, _: Any? -> BrowserUtil.browse(documentationSource.uri) }
            uriLink.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL))
            uriLink.setAlignmentX(Component.LEFT_ALIGNMENT)
            add(uriLink)
        }
        cardPanel.add(textLayoutContentBody, BorderLayout.CENTER)

        val rightActionsDeckPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false

            val viewOptionButtonLabel = JBLabel(AllIcons.Actions.Preview).apply {
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                border = JBUI.Borders.empty(0, 8)
            }

            val deleteActionTrashLabel = JBLabel(AllIcons.Actions.GC).apply {
                foreground = JBColor(Color(0xD32F2F), Color(0xE57373))
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                border = JBUI.Borders.empty(0, 8)

                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (dataModel.connectorDevelopmentType.documentationSource.contains(documentationSource)) {
                            dataModel.connectorDevelopmentType.documentationSource.remove(documentationSource.clone())
                        }
                        cardsContainer.remove(cardPanel)
                        cardsContainer.validate()
                        cardsContainer.repaint()
                    }
                })
            }

            add(viewOptionButtonLabel)
            add(deleteActionTrashLabel)
        }
        cardPanel.add(rightActionsDeckPanel, BorderLayout.EAST)

        return cardPanel
    }
}