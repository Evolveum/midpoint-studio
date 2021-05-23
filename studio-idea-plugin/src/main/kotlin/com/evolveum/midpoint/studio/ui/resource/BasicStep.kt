package com.evolveum.midpoint.studio.ui.resource

import com.intellij.ide.wizard.AbstractWizardStepEx
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.layout.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Created by Viliam Repan (lazyman).
 */
class BasicStep : AbstractWizardStepEx("Basic") {

    companion object {
        val ID = "basic"
    }

    var root: JPanel = panel {
        row {
            row("Name") {
                textField({ "Example Resource" }, {})
            }
            row("Connector") {
                ComboBox(DefaultComboBoxModel(arrayOf("asd", "asd")))()
            }
            row {
                buttonGroup("Type") {
                    row { checkBox("Source", comment = "Data synchronized to MidPoint") }
                    row { checkBox("Target", comment = "Data synchronized from MidPoint") }
                }
            }
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
        return ConnectorStep.ID
    }

    override fun getPreviousStepId(): Any? {
        return null
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
