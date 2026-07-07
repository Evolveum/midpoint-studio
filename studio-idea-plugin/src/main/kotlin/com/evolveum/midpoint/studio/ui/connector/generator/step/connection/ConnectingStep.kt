package com.evolveum.midpoint.studio.ui.connector.generator.step.connection

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class ConnectingStep(
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
            name = "Connecting"
        }
    }

    override fun _init() {
        super._init()
    }

    override fun _commit(finishChosen: Boolean) {
        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent = stepComponent
}