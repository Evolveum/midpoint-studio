/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

//package com.evolveum.midpoint.studio.ui.connector.generator.step
//
//import com.evolveum.midpoint.studio.impl.MidPointClient
//import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
//import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
//import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
//import com.intellij.openapi.ui.DialogPanel
//import com.intellij.ui.components.JBLabel
//import com.intellij.ui.dsl.builder.Align
//import com.intellij.ui.dsl.builder.AlignX
//import com.intellij.ui.dsl.builder.BottomGap
//import com.intellij.ui.dsl.builder.panel
//import com.intellij.util.ui.JBFont
//import javax.swing.JPanel
//
//class NextStep(
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
//            name = "Next"
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
//    private fun createPanel(): JPanel = panel {
//
//        row {
//            cell(JBLabel("What to do next?").apply {
//                font = JBFont.label().deriveFont(16f)
//            })
//        }
//
//        row {
//            text("""
//            You've successfully configured the basic search functionality for the User object type. Based on your progress, here are some recommended next steps you can take to extend or finalize your connector configuration.
//            """.trimIndent())
//                .align(AlignX.FILL)
//        }.bottomGap(BottomGap.MEDIUM)
//
//        separator()
//    }
//}