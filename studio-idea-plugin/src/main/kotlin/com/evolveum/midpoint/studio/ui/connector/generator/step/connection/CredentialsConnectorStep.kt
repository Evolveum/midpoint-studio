package com.evolveum.midpoint.studio.ui.connector.generator.step.connection

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevAuthInfoType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevHttpAuthTypeType
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

class CredentialsConnectorStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    dataModel : ConnectorGeneratorDataModel,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, dataModel, state, isHeader) {

    private val globalRadioBtnGroup = ButtonGroup()
    private var statusPanel = StatusPanel()
    private val mainPanel = JPanel(BorderLayout())
    private val credentialDynamicallyPanel = JPanel(BorderLayout())
    private val stepComponent: DialogPanel by lazy {
        panel {
            row {
                cell(mainPanel)
                    .align(Align.FILL)
            }.resizableRow()
        }.apply {
            name = "Credentials"
        }
    }

    override fun _init() {

        credentialDynamicallyPanel.add(panel {
            row {
                text("""
                You can optionally select an authentication method above to fill in testing credentials.
                """.trimIndent()).align(AlignX.FILL)
            }
        })

        mainPanel.add(createPanel())

        super._init()
    }

    override fun _commit(finishChosen: Boolean) {
        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent = stepComponent

    private fun createPanel(): JPanel = panel {

        row {
            cell(JBLabel("Authentication Method for Testing").apply {
                font = JBFont.label().deriveFont(16f)
            })
        }

        row {
            text("""
            Choose an authentication method for this connector and fill in the corresponding credential fields for testing. The fields below will update based on the selected method.
            """.trimIndent())
                .align(AlignX.FILL)
        }.bottomGap(BottomGap.MEDIUM)

        separator()

        row {
            cell(JBLabel("Method").apply {
                font = JBFont.label().deriveFont(16f)
            })
        }

        row {
            cell(createDynamicallyListPanel(dataModel.connectorDevelopmentType.connector.auth))
        }

        row {
            cell(JBLabel("Credentials").apply {
                font = JBFont.label().deriveFont(16f)
            }).align(Align.FILL)
        }

        row {
            cell(credentialDynamicallyPanel).align(Align.FILL)
        }
    }

    private fun createDynamicallyListPanel(listAuthInfoType: List<ConnDevAuthInfoType?>): JPanel = panel {

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
    }

    private fun createCardComponent(authInfo: ConnDevAuthInfoType): JComponent {

        val cardRadioButton = JBRadioButton()

        globalRadioBtnGroup.add(cardRadioButton)

        val cardPanel = JPanel(BorderLayout()).apply {
            border = getUnselectedBorder()
            preferredSize = Dimension(preferredSize.width, JBUI.scale(64))
            maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(64))
            putClientProperty("RADIO_COMPONENT", cardRadioButton)
        }

        val updateState = { isSelected: Boolean ->
            cardRadioButton.isSelected = isSelected
            cardPanel.border = if (isSelected) getSelectedBorder() else getUnselectedBorder()
            cardPanel.revalidate()
            cardPanel.repaint()
        }

        cardPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (!cardRadioButton.isSelected) {
                    updateState(true)
                    updateCredentialDynamicallyPanel(authInfo)
                }
            }
        })

        cardRadioButton.addItemListener { event ->
            val isSelected = event.stateChange == ItemEvent.SELECTED
            updateState(isSelected)

            if (isSelected) {
                updateCredentialDynamicallyPanel(authInfo)
            }
        }

        cardPanel.add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            add(cardRadioButton)
            add(Box.createRigidArea(Dimension(JBUI.scale(12), 0)))
        }, BorderLayout.WEST)

        cardPanel.add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false

            add(Box.createRigidArea(Dimension(0, JBUI.scale(8))))

            add(JBLabel(authInfo.name).apply {
                font = font.deriveFont(Font.BOLD, JBUI.scaleFontSize(13f).toFloat())
            })
        }, BorderLayout.CENTER)

        return cardPanel
    }

    private fun updateCredentialDynamicallyPanel(authInfo : ConnDevAuthInfoType) {
        credentialDynamicallyPanel.removeAll()
        credentialDynamicallyPanel.add(credentialsSectionForm(authInfo))
        credentialDynamicallyPanel.repaint()
        credentialDynamicallyPanel.revalidate()
    }

    private fun credentialsSectionForm(authInfo : ConnDevAuthInfoType): JComponent {

//        val attributes = SupportedAuthorization.attributesFor(dataModel.connectorDevelopmentType.connector.integrationType,
//            authInfo.type)

        if (authInfo.type.equals(ConnDevHttpAuthTypeType.BASIC)) {
            return panel {

                row {
                    label("Token")
                    icon(AllIcons.General.ContextHelp).apply {
                        component.toolTipText = "Enter your Token"
                    }
                }.topGap(TopGap.SMALL)

                row {
                    cell(JBPasswordField().apply {
                        text = ""
                        putClientProperty("JTextField.variant", "passwordWithoutCloud")
                    }).align(Align.FILL)
                }

                row {
                    label("Username")
                    icon(AllIcons.General.ContextHelp).apply {
                        component.toolTipText = "Enter your API key or account username here."
                    }
                }.topGap(TopGap.SMALL)

                row {
                    cell(JBTextField("apikey").apply {
                        putClientProperty("JTextField.trailingIcon", AllIcons.General.InlineRefresh)
                    }).align(Align.FILL)
                }
            }
        }

        return panel {}
    }
}