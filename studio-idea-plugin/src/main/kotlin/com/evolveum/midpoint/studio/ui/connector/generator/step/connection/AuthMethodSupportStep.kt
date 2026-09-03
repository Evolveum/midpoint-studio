/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.step.connection

import com.evolveum.midpoint.smart.api.conndev.SupportedAuthorization
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevAuthInfoType
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.panels.VerticalLayout
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import java.util.stream.Collectors
import javax.swing.*

class AuthMethodSupportStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {

    private var expandToggleBtn = JToggleButton()
    private val dynamicallyListPanel = JPanel(BorderLayout())

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Auth Method Support"
        ) {
            row {
                cell(JBLabel("Select Supported Authentication Methods").apply {
                    font = JBFont.label().deriveFont(16f)
                })
            }

            row {
                text("""
                Choose which authentication methods your connector should support. Users will be able to authenticate via any of the selected options when using this integration.
                """.trimIndent()).align(AlignX.FILL)
            }.bottomGap(BottomGap.MEDIUM)

            separator()

            row {
                cell(dynamicallyListPanel).align(Align.FILL)
            }
        }
    }

    override fun _init() {
        super._init()

        dataModel.connectorDevelopment.connector.auth.forEach {
            item -> dataModel.connectorDevelopment.application.auth.add(item.clone()) }

        refreshAuthOptions()

        expandToggleBtn.border = JBUI.Borders.empty()
        expandToggleBtn.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        expandToggleBtn.addActionListener { _: ActionEvent? -> refreshAuthOptions() }

        originalConnectorDevelopmentType = dataModel.connectorDevelopment.clone()
        canGoNext = true
    }

    override fun _commit(finishChosen: Boolean) {

        if (dataModel.connectorDevelopment.connector.auth.isEmpty()) {
            throw CommitStepException("Authentication method support selection is required.")
        }

        if (state == GenerateConnectorBadge.State.IN_PROGRESS ||
            state == GenerateConnectorBadge.State.EDITED
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
            state = GenerateConnectorBadge.State.COMPLETE
        }

        super._commit(finishChosen)
    }

    fun createDynamicallyListPanel(listAuthInfoType: List<ConnDevAuthInfoType?>): DialogPanel = panel {

        row {
            val cardsContainer = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                isOpaque = false

                listAuthInfoType.forEach { item ->
                    if (item != null) {
                        add(createCardComponent(item))
                        add(Box.createRigidArea(Dimension(0, JBUI.scale(8))))
                    }
                }
            }

            val scrollPane = JBScrollPane(cardsContainer).apply {
                border = JBUI.Borders.empty()
                viewportBorder = JBUI.Borders.empty()
                isOpaque = false
                viewport.isOpaque = false
                verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
                horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                verticalScrollBar.unitIncrement = JBUI.scale(16)
                val height = listAuthInfoType.size * 80
                preferredSize = Dimension(Int.MAX_VALUE,JBUI.scale(if (height >= 600) 600 else height))
            }

            cell(scrollPane).align(AlignX.FILL)
        }

        row {
            cell(expandToggleBtn).align(AlignX.CENTER)
        }
    }

    private fun createCardComponent(authInfo: ConnDevAuthInfoType): JComponent {

        val cardCheckbox = JBCheckBox()

        val cardPanel = JPanel(BorderLayout()).apply {
            border = getUnselectedBorder()
            preferredSize = Dimension(preferredSize.width,  JBUI.scale(64) )
            maximumSize = Dimension(Int.MAX_VALUE,  JBUI.scale(64) )
        }

        val updateState = { isSelected: Boolean ->
            cardCheckbox.isSelected = isSelected
            cardPanel.border = if (isSelected) getSelectedBorder() else getUnselectedBorder()
            cardPanel.revalidate()
            cardPanel.repaint()
        }

        updateState(authInfo in dataModel.connectorDevelopment.connector.auth)

        cardCheckbox.addItemListener { event ->
            val isSelected = event.stateChange == ItemEvent.SELECTED

            if (isSelected) dataModel.connectorDevelopment.connector.auth.add(authInfo.clone())
            else dataModel.connectorDevelopment.connector.auth.remove(authInfo)

            updateState(isSelected)
        }

        cardPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.source != cardCheckbox) {
                   updateState(!cardCheckbox.isSelected)
                }
            }
        })

        val leftActionsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            add(cardCheckbox)
            add(Box.createRigidArea(Dimension(JBUI.scale(12), 0)))
        }
        cardPanel.add(leftActionsPanel, BorderLayout.WEST)

        val textBodyLayout = JPanel(VerticalLayout(JBUI.scale(4))).apply {
            isOpaque = false

            val headerWrapper = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                isOpaque = false

                val titleLabel = JBLabel(authInfo.name).apply {
                    font = font.deriveFont(Font.BOLD, JBUI.scaleFontSize(13f).toFloat())
                }
                add(titleLabel)

                if (authInfo.isRecommended == true) {
                    add(Box.createRigidArea(Dimension(JBUI.scale(8), 0)))
                    add(GenerateConnectorBadge(GenerateConnectorBadge.Recommended.RECOMMENDED))
                }
            }
            add(headerWrapper)

            val descriptionLabel = JBLabel(authInfo.description).apply {
                font = font.deriveFont(JBUI.scaleFontSize(11.5f).toFloat())
                foreground = JBColor(0x666666, 0xBBBBBB)
            }
            add(descriptionLabel)
        }
        cardPanel.add(textBodyLayout, BorderLayout.CENTER)

        return cardPanel
    }

    private fun refreshAuthOptions() {
        val selected = expandToggleBtn.isSelected

        expandToggleBtn.setText(
            if (selected)
                "Hide not recommended options"
            else
                "Show all options"
        )

        dynamicallyListPanel.removeAll()
        dynamicallyListPanel.add(createDynamicallyListPanel(getValues(selected)))
        dynamicallyListPanel.revalidate()
        dynamicallyListPanel.repaint()
    }

    private fun getValues(showAllOptions: Boolean): List<ConnDevAuthInfoType?> {
        val values = dataModel.connectorDevelopment.application.auth

        if (!showAllOptions) {
            values.removeIf { v: ConnDevAuthInfoType? -> java.lang.Boolean.TRUE != v!!.isRecommended }
            return values
        }

        val existingTypes = values.stream()
            .map { obj: ConnDevAuthInfoType? -> obj!!.type }
            .collect(Collectors.toSet())

        Arrays.stream(SupportedAuthorization.entries.toTypedArray())
            .filter { auth: SupportedAuthorization? -> auth != SupportedAuthorization.NONE }
            .map { obj: SupportedAuthorization? -> obj!!.crateBasicInformation() }
            .filter { info: ConnDevAuthInfoType? -> existingTypes.add(info!!.type) }
            .forEach { e: ConnDevAuthInfoType? -> values.add(e) }

        return values
    }
}
