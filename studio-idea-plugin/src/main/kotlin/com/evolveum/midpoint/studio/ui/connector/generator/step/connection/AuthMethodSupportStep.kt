package com.evolveum.midpoint.studio.ui.connector.generator.step.connection

import com.evolveum.midpoint.smart.api.conndev.SupportedAuthorization
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
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
    dataModel : ConnectorGeneratorDataModel,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, dataModel, state, isHeader) {

    private val selectedOptions = mutableSetOf<ConnDevAuthInfoType>()
    private var toggleBtn = JToggleButton()

    private val mainPanel = JPanel(BorderLayout())
    private val dynamicallyListPanel = JPanel(BorderLayout())
    private val stepComponent: DialogPanel by lazy {
        panel {
            row {
                cell(mainPanel)
                    .align(Align.FILL)
            }.resizableRow()
        }.apply {
            name = "Auth Method Support"
        }
    }

    override fun _init() {
        mainPanel.add(createPanel())

        dataModel.connectorDevelopmentType.connector.auth.forEach { item -> selectedOptions.add(item.clone()) }

        refreshAuthOptions()

        toggleBtn.border = JBUI.Borders.empty()
        toggleBtn.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        toggleBtn.addActionListener { _: ActionEvent? -> refreshAuthOptions() }

        canGoNext(true)
        super._init()
    }

    override fun _commit(finishChosen: Boolean) {

        if (selectedOptions.isEmpty()) {
            throw CommitStepException("Authentication method support selection is required.")
        }

        try {
            dataModel.connectorDevelopmentType.connector.auth.clear()
            dataModel.connectorDevelopmentType.connector.auth.addAll(selectedOptions)

            dataModel.connectorDevelopmentType =
                upsertConnectorDevelopmentType(dataModel.connectorDevelopmentType)
        } catch (ex: Exception) {
            throw CommitStepException("Couldn't update connector development object. \n Error: ${ex.message}")
        }

        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent = stepComponent

    private fun createPanel(): JPanel = panel {

        row {
            cell(JBLabel("Select Supported Authentication Methods").apply {
                font = JBFont.label().deriveFont(16f)
            })
        }

        row {
            text("""
                Choose which authentication methods your connector should support. Users will be able to authenticate via any of the selected options when using this integration.
            """.trimIndent())
                .align(AlignX.FILL)
        }.bottomGap(BottomGap.MEDIUM)

        separator()

        row {
            cell(dynamicallyListPanel).align(Align.FILL)
        }
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
            cell(toggleBtn).align(AlignX.CENTER)
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

        if (selectedOptions.contains(authInfo)) {
            updateState(cardCheckbox.isSelected)
        }

        cardCheckbox.addItemListener { event ->
            val isSelected = event.stateChange == ItemEvent.SELECTED
            if (isSelected) selectedOptions.add(authInfo.clone()) else selectedOptions.remove(authInfo)
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
        val selected = toggleBtn.isSelected

        toggleBtn.setText(
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
        val values = dataModel.connectorDevelopmentType.application.auth

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
