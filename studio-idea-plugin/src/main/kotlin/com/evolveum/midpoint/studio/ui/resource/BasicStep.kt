package com.evolveum.midpoint.studio.ui.resource

import com.intellij.ide.wizard.AbstractWizardStepEx
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.layout.panel
import org.jetbrains.annotations.NotNull
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Created by Viliam Repan (lazyman).
 */
class BasicStep(model: ResourceWizardModel) : AbstractWizardStepEx("Basic") {

    companion object {
        val ID = "basic"
    }

    var model: ResourceWizardModel

    var root: JPanel

    init {
        this.model = model

        var resource = this.model.resource

        root = panel {
            row {
                row("Name") {
                    textField({ resource.name?.orig?.toString().orEmpty() }, { resource.name })
                }
                row("Description") {
                    textField({ resource.description?.toString().orEmpty() }, { resource.description })
                }
                row("Connector") {
                    ComboBox(DefaultComboBoxModel(arrayOf("asd", "asd")))()
                    button("Refresh", { e -> refreshConnectors(ModalityState.NON_MODAL) })
                }
                row {
                    buttonGroup("Type") {
                        row { checkBox("Source", comment = "Data synchronized to MidPoint") }
                        row { checkBox("Target", comment = "Data synchronized from MidPoint") }
                    }
                }
            }
        }
    }

    fun refreshConnectors(modalityState: @NotNull ModalityState) {
        ApplicationManager.getApplication().invokeLater({
            fireStateChanged()
        }, modalityState)
    }

    override fun getComponent(): JComponent {
        return root;
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return null
//        TODO("Not yet implemented")
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
        var resource = model.resource

        return !resource.name?.orig.isNullOrEmpty()
//        TODO("Not yet implemented")
    }

    @Suppress("EXPOSED_PARAMETER_TYPE")
    override fun commit(commitType: CommitType?) {
//        TODO("Not yet implemented")
    }
}
