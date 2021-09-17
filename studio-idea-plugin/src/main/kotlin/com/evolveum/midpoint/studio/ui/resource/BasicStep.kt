package com.evolveum.midpoint.studio.ui.resource

import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType
import com.intellij.ide.wizard.AbstractWizardStepEx
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import org.jetbrains.annotations.NotNull
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
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

    val root: JPanel

    lateinit var name: JBTextField
    lateinit var connector: ComboBox<ConnectorType>
    lateinit var source: JBCheckBox
    lateinit var target: JBCheckBox

    init {
        this.model = model

        val resource = this.model.resource

        root = panel {
            row {
                row("Name") {
                    name = textField(
                        { resource.name?.orig?.toString().orEmpty() },
                        { resource.name = PolyStringType.fromOrig(it) },
                    )
                        .component
                }
                row("Description") {
                    textField({ resource.description?.toString().orEmpty() }, { resource.description = it })
                }
                row("Connector") {
                    connector = ComboBox(DefaultComboBoxModel())
                    button("Refresh", { e -> refreshConnectors(ModalityState.NON_MODAL) })
                }
                row {
                    buttonGroup("Type") {
                        row { checkBox("Source", model.source, comment = "Data synchronized to MidPoint") }
                        row { checkBox("Target", model.target, comment = "Data synchronized from MidPoint") }
                    }
                }
            }
        }

        name.addKeyListener(object : KeyAdapter() {

            override fun keyReleased(e: KeyEvent?) {
                fireStateChanged()
            }
        })
        connector.addActionListener { fireStateChanged() }
    }

    fun refreshConnectors(modalityState: @NotNull ModalityState) {
        return
    }

    override fun getComponent(): JComponent {
        return root
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return name
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
        return !name?.text.isNullOrEmpty() && connector.selectedItem != null
    }

    @Suppress("EXPOSED_PARAMETER_TYPE")
    override fun commit(commitType: CommitType?) {
//        TODO("Not yet implemented")
    }
}
