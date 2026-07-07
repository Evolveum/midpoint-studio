package com.evolveum.midpoint.studio.ui.connector.generator.step.connection

import com.evolveum.midpoint.prism.PrismObject
import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.prism.path.ItemPath
import com.evolveum.midpoint.schema.constants.SchemaConstants
import com.evolveum.midpoint.studio.client.MidPointObject
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.impl.SearchOptions
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevHttpEndpointType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType
import com.evolveum.prism.xml.ns._public.types_3.RawType
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
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ItemEvent
import javax.swing.*

class TestConnectionStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    dataModel : ConnectorGeneratorDataModel,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, dataModel, state, isHeader) {

    private lateinit var prismObjectResource : PrismObject<ResourceType>

    private val globalRadioBtnGroup = ButtonGroup()
    private val endpointsListPanel = JPanel(BorderLayout())
    private val statusPanel = StatusPanel()
    private val mainPanel = JPanel(BorderLayout())
    private val stepComponent: DialogPanel by lazy {
        panel {
            row {
                cell(mainPanel)
                    .align(Align.FILL)
            }.resizableRow()
        }.apply {
            name = "Test Connection"
        }
    }

    init {
        val prismObject = getPrismObjectResource()

        if (prismObject != null) {
            prismObjectResource = prismObject
        } else {
            // TODO
        }
    }

    override fun _init() {

        endpointsListPanel.add(createEndpointsListPanelPanel(
            dataModel.connectorDevelopmentType.testing.suggestedEndpoint
        ))

        mainPanel.add(createPanel())

        canGoNext(true)

        super._init()
    }

    override fun _commit(finishChosen: Boolean) {

        try {
            val resultUpsert = client.upsert(prismObjectResource, null)
                ?: throw CommitStepException("PrismObject resource not found")

            client.testResource(resultUpsert.oid)
        } catch (ex: Exception) {
            throw CommitStepException("Couldn't update Resource object. \n Error: " + ex.message)
        }

        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent = stepComponent

    private fun createPanel(): JPanel = panel {

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

    private fun createEndpointsListPanelPanel(listHttpEndpointType: List<ConnDevHttpEndpointType?>): JPanel = panel {

        row {
            val cardsContainer = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                isOpaque = false

                var currentRestTextEndpoint = getRestTestEndpointProp()
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
                    !hasMatch && currentRestTextEndpoint != null,
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
        restUriEndpoint: String?,
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

                    verticalContainer.add(JBTextField(restUriEndpoint).apply {
                        name = ENTER_URL_MANUALLY_TEXT_FIELD_ID
                        font = font.deriveFont(Font.BOLD, JBUI.scaleFontSize(13f).toFloat())
                        alignmentX = JPanel.LEFT_ALIGNMENT
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

                    setRestTestEndpointProp(restUriEndpoint)
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

                verticalContainer.add(JBTextField(restUriEndpoint).apply {
                    name = ENTER_URL_MANUALLY_TEXT_FIELD_ID
                    font = font.deriveFont(Font.PLAIN, JBUI.scaleFontSize(13f).toFloat())
                    alignmentX = JPanel.LEFT_ALIGNMENT

                    maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
                })
            }
        } else {
            restUriEndpoint?.let { endpointText ->
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

    private fun <O : ObjectType> getClassFromReference(objectReference: ObjectReferenceType?): Class<O>? {
        if (objectReference?.type == null) return null
        val prismContext = client.prismContext
        val standardClass = prismContext.schemaRegistry
            .determineCompileTimeClass<ObjectType>(objectReference.type)

        @Suppress("UNCHECKED_CAST")
        return standardClass as? Class<O>
    }

    private fun getPrismObjectResource() : PrismObject<ResourceType>? {
        dataModel.connectorDevelopmentType.testing?.let { testing ->
            val midpointObject : MidPointObject = client.get(
                getClassFromReference<ResourceType>(testing.testingResource),
                testing.testingResource.oid,
                SearchOptions().raw(false)
            )

            return client.prismContext.parseObject(midpointObject.content)
        }

        return null
    }

    private fun getRestTestEndpointProp() : String? {
        return when (val realValue = prismObjectResource.findProperty<Any>(REST_TEST_ENDPOINT_ITEM_PATH)?.realValue) {
            is String -> realValue
            is RawType -> realValue.value as? String
            else -> null
        }
    }

    private fun setRestTestEndpointProp(restUriEndpoint: String?) {
        prismObjectResource.findOrCreateProperty<Any>(REST_TEST_ENDPOINT_ITEM_PATH).realValue =
            restUriEndpoint
    }

    companion object {
        private const val ENTER_URL_MANUALLY_CARD_ID = "urlManuallyCard"
        private const val ENTER_URL_MANUALLY_TEXT_FIELD_ID = "urlManuallyTextField"
        private const val ENTER_URL_MANUALLY_LABEL = "Enter URL manually"
        private val REST_TEST_ENDPOINT_ITEM_PATH = ItemPath.create(
            ResourceType.F_CONNECTOR_CONFIGURATION,
            SchemaConstants.ICF_CONFIGURATION_PROPERTIES_NAME,
            ItemName.from("", "restTestEndpoint")
        )
    }
}