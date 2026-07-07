package com.evolveum.midpoint.studio.ui.connector.generator.step

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBFont
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class InitialContinueConnectorDevelopmentStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    dataModel : ConnectorGeneratorDataModel,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, dataModel, state, isHeader) {

    private val mainPanel = JPanel(BorderLayout())
    private val stepComponent: DialogPanel by lazy {
        panel {
            row {
                cell(mainPanel)
                    .align(Align.FILL)
            }.resizableRow()
        }.apply {
            name = "Connector Development continue"
        }
    }

    override fun _init() {
        mainPanel.add(createPanel())
        canGoNext(true)

        super._init()
    }

    override fun _commit(finishChosen: Boolean) {
        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent {
        return stepComponent
    }

    private fun createPanel(): JPanel = panel {

        row {
            cell(JBLabel("Connector Development continue").apply {
                font = JBFont.label().deriveFont(16f)
            })
        }

        row {
            text("Initial Connector development step".trimIndent()).align(AlignX.FILL)
        }.bottomGap(BottomGap.MEDIUM)
    }
}