/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.connection

import com.evolveum.midpoint.prism.PrismObject
import com.evolveum.midpoint.prism.PrismPropertyDefinition
import com.evolveum.midpoint.prism.path.ItemPath
import com.evolveum.midpoint.schema.constants.SchemaConstants
import com.evolveum.midpoint.schema.util.ConnectorTypeUtil
import com.evolveum.midpoint.smart.api.conndev.SupportedAuthorization
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.development.util.ConnectorDevelopmentWizardUtil
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.studio.ui.prism.PrismDefinitionPanelBuilder
import com.evolveum.midpoint.xml.ns._public.common.common_3.*
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.NotNull
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ItemEvent
import javax.swing.*
import javax.xml.namespace.QName

class CredentialsConnectorStep(
    wizardContext : ConnectorDevelopmentWizard,
    client : MidPointClient,
    state : Badge.State,
    isHeader : Boolean
) : ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    private val globalRadioBtnGroup = ButtonGroup()
    private val credentialDynamicallyPanel = JPanel(BorderLayout())
    private val prismDefinitionPanelBuilder = PrismDefinitionPanelBuilder()

    override val dialogPanel: DialogPanel by lazy {

        createDialogPanel(
            "Credentials"
        ) {

            row {
                cell(JBLabel("Authentication Method for Testing").apply {
                    font = JBFont.label().deriveFont(16f)
                })
            }

            row {
                text(
                    """
                    Choose an authentication method for this connector and fill in the corresponding credential fields for testing. The fields below will update based on the selected method.
                    """.trimIndent()
                ).align(AlignX.FILL)
            }.bottomGap(BottomGap.MEDIUM)

            separator()

            row {
                cell(JBLabel("Method").apply {
                    font = JBFont.label().deriveFont(16f)
                })
            }

            row {
                cell(createAuthMethodListPanel(dataModel.connectorDevelopment.connector.auth))
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
    }

    override fun _init() {
        super._init()

        continueFrom()

        ConnectorDevelopmentWizardUtil.getTestingResource(client, dataModel.connectorDevelopment)

        credentialDynamicallyPanel.add(panel {
            row {
                text("""
                You can optionally select an authentication method above to fill in testing credentials.
                """.trimIndent()).align(AlignX.FILL)
            }
        })
    }

    override fun _commit(finishChosen: Boolean) {
        super._commit(finishChosen)

//        apiKey.requireNotBlank("API key")
//        tokenValue.requireNotBlank("token value")

        dataModel.connectorDevelopment.application

        try {
            val resource = getObjectByOid(
                dataModel.connectorDevelopment.testing.testingResource.oid,
                ResourceType::class.java
            )

            setConfigurationProperties(resource, "basic")

            if (state == Badge.State.IN_PROGRESS ||
                state == Badge.State.EDITED
            ) {
                dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
                updateConnectorDevelopment()
                state = Badge.State.COMPLETE
            }

        } catch (e : Exception) {
            throw CommitStepException(e.message)
        }
    }

    private fun createAuthMethodListPanel(listAuthInfoType: List<ConnDevAuthInfoType?>): JPanel = panel {

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

        cardRadioButton.addItemListener { event ->
            val isSelected = event.stateChange == ItemEvent.SELECTED
            updateState(isSelected)

            if (isSelected) {
                updateCredentialDynamicallyPanel(authInfo)
                canGoNext = true
            } else {
                canGoNext = false
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

        try {
            val integrationType = dataModel.connectorDevelopment.application.integrationType
            val itemNames = SupportedAuthorization.attributesFor(integrationType,authInfo.type)
            val connectorObject = ConnectorDevelopmentWizardUtil.getConnectorType(client, dataModel.connectorDevelopment)
            val configConnector = ConnectorDevelopmentWizardUtil.findConnectorConfigurationPropertiesDefinition(
                connectorObject
            )

            val prismDefinitionPanelBuilder = PrismDefinitionPanelBuilder()

            if (configConnector != null) {
                credentialDynamicallyPanel.removeAll()
                credentialDynamicallyPanel.add(prismDefinitionPanelBuilder.build(
                    itemNames,
                    configConnector
                ))
                credentialDynamicallyPanel.repaint()
                credentialDynamicallyPanel.revalidate()
            }
        } catch (e: Exception) {
            statusPanel.status = StatusPanel.Status.ERROR
            replaceContent(
               getAlertComponent(
                   statusPanel,
                   statusPanel.status?.name?: "Error",
                   e.message
               )
            )
        }
    }

    private fun setConfigurationProperties(
        @NotNull resource: PrismObject<ResourceType>,
        @NotNull property: String
    ) {

        val connector = getObjectByOid(
            dataModel.connectorDevelopment.connector.connectorRef.oid,
            ConnectorType::class.java
        )

        val connectorSchema = ConnectorTypeUtil.parseConnectorSchema(
            connector.asObjectable()
        )

        val itemDefinition = connectorSchema.findItemDefinitionByElementName(
            QName("connectorConfiguration")
        )

        val propertyDefinition = itemDefinition.findItemDefinition(
            ItemPath.create(
                SchemaConstants.ICF_CONFIGURATION_PROPERTIES_LOCAL_NAME,
                property
            ),
            PrismPropertyDefinition::class.java
        )

        val resourceConfigurationProperties = resource.findOrCreateProperty<PrismPropertyDefinition<*>>(
            ItemPath.create(
                ResourceType.F_CONNECTOR_CONFIGURATION,
                SchemaConstants.ICF_CONFIGURATION_PROPERTIES_LOCAL_NAME,
                propertyDefinition
            )
        )
    }
}