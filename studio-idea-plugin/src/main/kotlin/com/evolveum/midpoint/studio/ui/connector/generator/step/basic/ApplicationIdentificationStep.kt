package com.evolveum.midpoint.studio.ui.connector.generator.step.basic

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.DocumentAdapter
import com.intellij.util.ui.JBFont
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

class ApplicationIdentificationStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    dataModel : ConnectorGeneratorDataModel,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, dataModel, state, isHeader) {

    private var applicationName = ""
    private var description = ""
    private var versionOfApplication = ""
    private var integrationType = COMBO_BOX_ITEM_UNDEFINED
    private var deploymentType = COMBO_BOX_ITEM_UNDEFINED
    private val stepComponent: DialogPanel by lazy {
        panel {
            row {
                cell(JBLabel("Identify the target application").apply {
                    font = JBFont.label().deriveFont(16f)
                })
            }

            row {
                text("Tell us which application you want to connect to. Based on this information, the system will identify the target and locate appropriate documentation.")
                    .align(AlignX.FILL)
            }

            separator()

            row("Application name *") {
                textField()
                    .align(AlignX.FILL)
                    .bindText({ applicationName }, { applicationName = it })
                    .component.apply {
                        document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                applicationName = text
                            }
                        })
                    }
            }

            row("Description") {
                scrollCell(textArea().apply {
                    component.rows = 5
                    component.lineWrap = true
                    component.wrapStyleWord = true
                    visible(false)
                }.component)
                    .align(Align.FILL)
                    .bindText({ description }, { description = it })
                    .component.apply {
                        document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                description = text
                            }
                        })
                    }
            }

            row("Version of application") {
                textField()
                    .align(AlignX.FILL)
                    .bindText({ versionOfApplication }, { versionOfApplication = it })
                    .component.apply {
                        document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                versionOfApplication = text
                            }
                        })
                    }

            }

            row("Integration Type *") {
                comboBox(listOf(COMBO_BOX_ITEM_UNDEFINED) + ConnDevIntegrationType.entries.map { it.name })
                    .bindItem({ integrationType }, { integrationType = it ?: COMBO_BOX_ITEM_UNDEFINED })
                    .component.apply {
                        addActionListener {
                            val selected = selectedItem as? String
                            if (selected != null) integrationType = selected
                        }
                    }
            }

            row("Deployment type") {
                comboBox(listOf(COMBO_BOX_ITEM_UNDEFINED) + ConnDevDeploymentType.entries.map { it.name })
                    .bindItem({ deploymentType }, { deploymentType = it ?: COMBO_BOX_ITEM_UNDEFINED })
                    .component.apply {
                        addActionListener {
                            val selected = selectedItem as? String
                            if (selected != null) deploymentType = selected
                        }
                    }
            }
        }.apply {
            name = "Application Identification"
        }
    }

    override fun _init() {

        if (getState() != GenerateConnectorBadge.State.COMPLETE) {
            setState(GenerateConnectorBadge.State.IN_PROGRESS)
        }
        canGoNext(true)

        dataModel.connectorDevelopmentType?.application?.let { application ->
            applicationName = application.applicationName.orig
            description = application.description
            versionOfApplication = application.version
            integrationType = application.integrationType?.name ?: COMBO_BOX_ITEM_UNDEFINED
            deploymentType = application.deploymentType?.name ?: COMBO_BOX_ITEM_UNDEFINED
        }

        stepComponent.reset()

        super._init()
    }

    override fun _commit(finishChosen: Boolean) {
        stepComponent.apply()

        if (applicationName.isBlank()) {
            throw CommitStepException("Field Application Name is required")
        }

        if (integrationType == COMBO_BOX_ITEM_UNDEFINED) {
            throw CommitStepException("Field Integration Type is required")
        }

        if (getState() == GenerateConnectorBadge.State.IN_PROGRESS ||
            getState() == GenerateConnectorBadge.State.EDITED
        ) {
            try {
                val connectorDevelopmentType = ConnectorDevelopmentType()
                connectorDevelopmentType.application = getConnDevApplicationInfoType()
                connectorDevelopmentType.name = connectorDevelopmentType.application.applicationName

                dataModel.connectorDevelopmentType =
                    upsertConnectorDevelopmentType(connectorDevelopmentType)
            } catch (ex: Exception) {
                throw CommitStepException("Couldn't update connector development object. \n Error: " + ex.message)
            }
        }

        setState(GenerateConnectorBadge.State.COMPLETE)

        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent = stepComponent

    private fun getConnDevApplicationInfoType(): ConnDevApplicationInfoType {
        val connDevApplicationInfoType = ConnDevApplicationInfoType()
        connDevApplicationInfoType.applicationName = PolyStringType(applicationName)
        connDevApplicationInfoType.version = versionOfApplication
        connDevApplicationInfoType.description = description

        integrationType.takeIf { it != COMBO_BOX_ITEM_UNDEFINED }?.let {
            connDevApplicationInfoType.integrationType = ConnDevIntegrationType.fromValue(it.lowercase())
        }

        deploymentType.takeIf { it != COMBO_BOX_ITEM_UNDEFINED }?.let {
            connDevApplicationInfoType.deploymentType = ConnDevDeploymentType.fromValue(it.lowercase())
        }

        return connDevApplicationInfoType
    }

    companion object {
        private const val COMBO_BOX_ITEM_UNDEFINED = "Undefined"
    }
}