package com.evolveum.midpoint.studio.ui.resource

import com.intellij.ide.wizard.AbstractWizardStepEx
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Created by Viliam Repan (lazyman).
 */
class ConnectorStep : AbstractWizardStepEx("Connector") {

    companion object {
        val ID = "connector"
    }

    var root: JPanel = panel {
        row {
            row("Connector Configuration", separated = true) {

            }
            row("Pooling", separated = true) {

            }
            row("Handlers") {

            }
        }
    }

    override fun getComponent(): JComponent {
        return root
    }

    override fun getPreferredFocusedComponent(): JComponent? {
//        TODO("Not yet implemented")
        return null
    }

    override fun getStepId(): Any {
        return ID
    }

    override fun getNextStepId(): Any? {
        return CapabilitiesStep.ID
    }

    override fun getPreviousStepId(): Any? {
        return BasicStep.ID
    }

    override fun isComplete(): Boolean {
//        TODO("Not yet implemented")
        return true
    }

    @Suppress("EXPOSED_PARAMETER_TYPE")
    override fun commit(commitType: CommitType?) {
//        TODO("Not yet implemented")
    }
}
