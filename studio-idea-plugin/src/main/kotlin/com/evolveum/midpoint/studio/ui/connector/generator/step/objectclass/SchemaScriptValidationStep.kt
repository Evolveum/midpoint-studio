/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

//package com.evolveum.midpoint.studio.ui.connector.generator.step.objectclass
//
//import com.evolveum.midpoint.studio.impl.MidPointClient
//import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
//import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
//import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
//import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep
//import com.intellij.openapi.ui.DialogPanel
//import com.intellij.ui.components.JBLabel
//import com.intellij.ui.dsl.builder.Align
//import com.intellij.ui.dsl.builder.AlignX
//import com.intellij.ui.dsl.builder.BottomGap
//import com.intellij.ui.dsl.builder.panel
//import com.intellij.util.ui.JBFont
//import javax.swing.JComponent
//import javax.swing.JPanel
//
//class SchemaScriptValidationStep(
//    wizardContext : ConnectorGeneratorWizard,
//    client : MidPointClient,
//    state : GenerateConnectorBadge.State,
//    isHeader : Boolean
//) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {
//
//    private val statusPanel = StatusPanel()
//    private val mainPanel: DialogPanel by lazy {
//        panel {
//            row {
//                cell(mainPanel)
//                    .align(Align.FILL)
//            }.resizableRow()
//        }.apply {
//            name = "Schema Script Validation"
//        }
//    }
//
//    override fun getMainPanel(): JPanel {
//        return mainPanel
//    }
//
//    override fun getStatusPanel(): StatusPanel {
//        return statusPanel
//    }
//
//    override fun _init() {
//        super._init()
//    }
//
//    override fun _commit(finishChosen: Boolean) {
//        super._commit(finishChosen)
//    }
//
//    override fun getComponent(): JComponent = mainPanel
//
//    private fun createPanel(): JPanel = panel {
//
//        row {
//            cell(JBLabel("Schema scripts validation").apply {
//                font = JBFont.label().deriveFont(16f)
//            })
//        }
//
//        row {
//            text("""
//            This step verifies that the schema scripts are correctly defined and executable, ensuring the connector can properly interpret and process the data structure.
//            """.trimIndent())
//                .align(AlignX.FILL)
//        }.bottomGap(BottomGap.MEDIUM)
//
//        separator()
//    }
//}