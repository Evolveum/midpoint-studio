/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator

import com.evolveum.midpoint.studio.client.AuthenticationException
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.dataModel.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.navigation.NavigationItem
import com.evolveum.midpoint.studio.ui.connector.generator.navigation.NavigationNode
import com.evolveum.midpoint.studio.ui.connector.generator.navigation.NavigationTree
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.util.exception.SchemaException
import com.intellij.ide.wizard.AbstractWizard
import com.intellij.ide.wizard.CommitStepException
import com.intellij.ide.wizard.Step
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.NonNls
import java.awt.BorderLayout
import java.awt.Font
import java.io.IOException
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel
import javax.swing.ScrollPaneConstants
import javax.swing.UIManager


abstract class ConnectorGeneratorWizard(
    project: Project
) : AbstractWizard<Step>("Connector Generator", project) {

    protected val log = Logger.getInstance(this.javaClass)

    /**
     * The navigation forest.
     *
     * This is NOT the AbstractWizard list.
     */
    protected val wizardStepsList = NavigationTree()

    /**
     * Flat list displayed by Swing.
     */
    protected val visibleListModel = DefaultListModel<NavigationItem>()

    /**
     * Actual navigation JList.
     */
    protected val stepNavigationItems = JBList(visibleListModel)

    /**
     * Maps:
     *
     * JList index
     *      ↓
     * AbstractWizard step index
     */
    protected val navIndexToStepIndexMap = mutableListOf<Int>()

    abstract val dataModel: ConnectorGeneratorDataModel

    var isLeavingStepByPreviousTouch:
            Boolean = false
        protected set

    @Throws(
        SchemaException::class,
        AuthenticationException::class,
        IOException::class
    ) protected abstract fun buildSteps()

    @NonNls
    override fun getHelpID(): @NonNls String? = ""

    override fun updateButtons(
        lastStep: Boolean,
        canGoNext: Boolean,
        firstStep: Boolean
    ) {

        super.updateButtons(
            lastStep,
            canGoNext,
            firstStep
        )

        val currentStep =
            wizardStepsList[currentStep]

        nextButton?.text =
            currentStep.nextButtonText
    }

    override fun getNextButton(): JButton? {
        return super.getNextButton()
    }

    /**
     * Registers all actual wizard steps with AbstractWizard.
     *
     * IMPORTANT:
     *
     * Categories are NOT added.
     */
    protected fun registerWizardSteps() {

        wizardStepsList
            .flattenSteps()
            .forEach { step ->
                addStep(step)
            }
    }

    /**
     * Returns all actual wizard steps.
     */
    private fun wizardSteps(): List<ConnectorGeneratorGeneralWizardStep> = wizardStepsList.flattenSteps()

    /**
     * Gets a wizard step safely.
     */
    private fun stepAt(index: Int ): ConnectorGeneratorGeneralWizardStep? {
        return wizardSteps().getOrNull(index)
    }

    override fun createCenterPanel(): JComponent {

        updateNavigationMenuByLiveStates()
        configureNavigationRenderer()
        configureNavigationList()
        configureNavigationListener()

        val splitter = JBSplitter(false, 0.25f)
        splitter.border = JBUI.Borders.empty()
        splitter.firstComponent = stepNavigationItems

        val mainPanel = ScrollPaneFactory.createScrollPane(super.createCenterPanel())
        mainPanel.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        mainPanel.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
        mainPanel.border = JBUI.Borders.empty()
        splitter.secondComponent = mainPanel

        return splitter
    }

    private fun configureNavigationRenderer() {

        stepNavigationItems.cellRenderer =
            ListCellRenderer {
                    _: JList<out NavigationItem>?,
                    value: NavigationItem?,
                    _: Int,
                    isSelected: Boolean,
                    _: Boolean ->

                createNavigationCell(
                    value,
                    isSelected
                )
            }
    }

    private fun createNavigationCell(
        value: NavigationItem?,
        isSelected: Boolean
    ): JPanel {

        val panel = JPanel(BorderLayout())
        val label = JLabel()

        when (value) {

            is NavigationItem.Header -> {

                label.text = value.name
                label.icon = UIManager.getIcon(
                    if (value.expanded) "Tree.expandedIcon" else "Tree.collapsedIcon"
                )

                label.font = UIUtil.getLabelFont(
                        UIUtil.FontSize.NORMAL).deriveFont(
                            Font.BOLD,
                            JBUI.scaleFontSize(
                                14f
                            ).toFloat()
                        )
                label.foreground = UIUtil.getContextHelpForeground()

                panel.isOpaque = true
                panel.border =
                    JBUI.Borders.empty(
                        12,
                        10,
                        4,
                        10
                    )
            }

            is NavigationItem.Step -> {

                label.text = value.name
                label.icon = null

                if (isSelected) {
                    label.foreground = UIUtil.getListSelectionForeground(true)

                    label.font =
                        UIUtil.getLabelFont()
                            .deriveFont(
                                Font.BOLD,
                                JBUI.scaleFontSize(
                                    14f
                                ).toFloat()
                            )

                } else {

                    label.foreground = UIUtil.getLabelForeground()
                    label.font =
                        UIUtil.getLabelFont()
                            .deriveFont(
                                Font.PLAIN,
                                JBUI.scaleFontSize(
                                    14f
                                ).toFloat()
                            )
                }

                panel.isOpaque = true
                panel.border = JBUI.Borders.empty(
                        8,
                        22,
                        8,
                        10
                    )
            }

            null -> label.text = ""
        }

        panel.add(
            label,
            BorderLayout.CENTER
        )

        if (value is NavigationItem.Step) {

            val badge =
                GenerateConnectorBadge(
                    value.state
                )

            panel.add(
                badge,
                BorderLayout.EAST
            )
        }

        return panel
    }

    private fun configureNavigationList() {

        stepNavigationItems.border =
            JBUI.Borders.empty()

        stepNavigationItems.selectionMode =
            ListSelectionModel.SINGLE_SELECTION

        stepNavigationItems.setSelectionBackground(
            UIUtil.TRANSPARENT_COLOR
        )

        stepNavigationItems.fixedCellHeight =
            -1
    }

    private fun configureNavigationListener() {

        stepNavigationItems.addListSelectionListener { event ->
            if (!event.valueIsAdjusting) {
                handleNavigationSelection()
            }
        }
    }

    private fun handleNavigationSelection() {

        val navigationIndex = stepNavigationItems.selectedIndex

        if (navigationIndex < 0) return

        if (navigationIndex >= visibleListModel.size) return

        when (val item = visibleListModel.getElementAt(navigationIndex)) {

            is NavigationItem.Header -> {
                item.node.expanded = !item.node.expanded
                updateNavigationMenuByLiveStates()
            }

            is NavigationItem.Step -> {
                navigateToStep(item.wizardStepIndex)
            }
        }
    }

    private fun navigateToStep(stepIndex: Int) {

        if (stepIndex !in wizardSteps().indices || stepIndex == currentStep) return

        val previousStep = stepAt(currentStep)

        if (previousStep?.state == GenerateConnectorBadge.State.EDITED) {
            previousStep.state = GenerateConnectorBadge.State.COMPLETE
        }

        myCurrentStep = stepIndex
        updateStep()
        synchronizeMenuHighlight()
    }

    override fun doNextAction() {
        isLeavingStepByPreviousTouch = false
        super.doNextAction()
        updateNavigationMenuByLiveStates()
    }

    override fun doPreviousAction() {
        isLeavingStepByPreviousTouch = true
        super.doPreviousAction()
        synchronizeMenuHighlight()
    }

    override fun canGoNext(): Boolean = stepAt(currentStep)
        ?.canGoNext
        ?: false

    override fun canFinish(): Boolean = stepAt(currentStep)
        ?.canGoNext
        ?: false

    fun updateNavigationMenuByLiveStates() {

        visibleListModel.clear()
        navIndexToStepIndexMap.clear()

        fun addNode(node: NavigationNode) {

            when (node) {

                is NavigationNode.Category -> {

                    // Only show category if it contains at least one visible child.
                    if (!wizardStepsList.hasVisibleChildren(node)) return

                    visibleListModel.addElement(
                        NavigationItem.Header(node)
                    )

                    navIndexToStepIndexMap.add(-1)

                    if (node.expanded) {
                        node.children.forEach(::addNode)
                    }
                }

                is NavigationNode.Step -> {

                    val step = node.step

                    if (
                        step.state ==
                        GenerateConnectorBadge.State.NONE
                    ) {
                        return
                    }

                    val stepIndex =
                        wizardStepsList.indexOf(step)

                    if (stepIndex < 0) {
                        return
                    }

                    visibleListModel.addElement(
                        NavigationItem.Step(
                            node,
                            stepIndex
                        )
                    )

                    navIndexToStepIndexMap.add(
                        stepIndex
                    )
                }
            }
        }

        wizardStepsList.roots.forEach(::addNode)

        synchronizeMenuHighlight()
    }

    private fun synchronizeMenuHighlight() {
        val navigationIndex = navIndexToStepIndexMap.indexOf(currentStep)

        if (navigationIndex >= 0 && navigationIndex < visibleListModel.size) {
            stepNavigationItems.selectedIndex = navigationIndex
        }
    }
}