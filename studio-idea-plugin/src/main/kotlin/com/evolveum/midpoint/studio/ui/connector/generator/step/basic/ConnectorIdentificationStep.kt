package com.evolveum.midpoint.studio.ui.connector.generator.step.basic

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevConnectorType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBFont
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

class ConnectorIdentificationStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    dataModel : ConnectorGeneratorDataModel,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, dataModel, state, isHeader) {

    private var groupId = ""
    private var artifactId = ""
    private var displayName = ""
    private var description = ""
    private var version = ""
    private val stepComponent: DialogPanel by lazy {
        panel {
            row {
                cell(JBLabel("Set Basic Information About the Connector").apply {
                    font = JBFont.label().deriveFont(16f)
                })
            }

            row {
                text("On this panel you can enter the fundamental details that describe the connector, providing the necessary context before adding more specific configuration.")
                    .align(AlignX.FILL)
            }

            separator()

            row("Group id *") {
                textField()
                    .align(AlignX.FILL)
                    .bindText({ groupId }, { groupId = it })
                    .component.apply {
                        document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                groupId = text
                            }
                        })
                    }
            }

            row("Artifact id *") {
                textField()
                    .align(AlignX.FILL)
                    .bindText({ artifactId }, { artifactId = it })
                    .component.apply {
                        document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                artifactId = text
                            }
                        })
                    }

            }

            row("Display name") {
                textField()
                    .align(AlignX.FILL)
                    .bindText({ displayName }, { displayName = it })
                    .component.apply {
                        document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                displayName = text
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

            row("Version *") {
                textField()
                    .align(AlignX.FILL)
                    .bindText({ version }, { version = it })
                    .component.apply {
                        document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                version = text
                            }
                        })
                    }
            }
        }.apply {
            name = "Connector identification"
        }
    }

    override fun _init() {

        if (getState() != GenerateConnectorBadge.State.COMPLETE) {
            setState(GenerateConnectorBadge.State.IN_PROGRESS)
        }

        canGoNext(true)

        dataModel.connectorDevelopmentType?.let { conDevType ->
            val connector = conDevType.connector

            groupId = connector?.groupId?.takeIf { it.isNotBlank() }
                ?: "com.evolveum.polygon.community"
            artifactId = connector?.artifactId?.takeIf { it.isNotBlank() }
                ?: conDevType.application.applicationName.getNorm()
            displayName = connector?.displayName?.getNorm() ?: ""
            description = connector?.description ?: ""
            version = connector?.version?.takeIf { it.isNotBlank() }
                ?: "1.0"
        }

        stepComponent.reset()

        super._init()
    }

    override fun _commit(finishChosen: Boolean) {
        stepComponent.apply()

        if (groupId.isBlank()) {
            throw CommitStepException("Field Group Id is required")
        }

        if (artifactId.isBlank()) {
            throw CommitStepException("Field Artifact Id is required")
        }

        if (version.isBlank()) {
            throw CommitStepException("Field Version is required")
        }

        if (getState() == GenerateConnectorBadge.State.IN_PROGRESS ||
            getState() == GenerateConnectorBadge.State.EDITED
        ) {
            try {
                val connectorDevelopmentType: ConnectorDevelopmentType = dataModel.connectorDevelopmentType
                val connDevConnectorType: ConnDevConnectorType = getConnDevConnectorType()
                connectorDevelopmentType.connector = connDevConnectorType
                connectorDevelopmentType.name = PolyStringType.fromOrig(
                    (connDevConnectorType.groupId
                            + ":" + connDevConnectorType.artifactId
                            + ":" + connDevConnectorType.version)
                )

                dataModel.connectorDevelopmentType =
                    upsertConnectorDevelopmentType(connectorDevelopmentType)
            } catch (ex: Exception) {
                throw CommitStepException("Couldn't update connector development object. \n Error: ${ex.message}")
            }

            setState(GenerateConnectorBadge.State.COMPLETE)
        }

        super._commit(finishChosen)
    }

    override fun getComponent(): JComponent = stepComponent

    private fun getConnDevConnectorType(): ConnDevConnectorType {
        val connDevConnectorType = ConnDevConnectorType()
        connDevConnectorType.groupId = groupId
        connDevConnectorType.artifactId = artifactId
        connDevConnectorType.displayName = PolyStringType(displayName)
        connDevConnectorType.description = description
        connDevConnectorType.version = version
        return connDevConnectorType
    }
}