package com.evolveum.midpoint.studio.ui.resource

import com.intellij.ide.wizard.AbstractWizardStepEx
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Created by Viliam Repan (lazyman).
 */
class CapabilitiesStep : AbstractWizardStepEx("Capabilities") {

    companion object {
        val ID = "capabilities"
    }

    var root: JPanel = panel {
        row {

        }
    }

    override fun getComponent(): JComponent {
        return root;
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return null
        TODO("Not yet implemented")
    }

    override fun getStepId(): Any {
        return ID
    }

    override fun getNextStepId(): Any? {
        return ObjectTypesStep.ID
    }

    override fun getPreviousStepId(): Any? {
        return ConnectorStep.ID
    }

    override fun isComplete(): Boolean {
        return true
        TODO("Not yet implemented")
    }

    @Suppress("EXPOSED_PARAMETER_TYPE")
    override fun commit(commitType: CommitType?) {
//        TODO("Not yet implemented")
    }
}
