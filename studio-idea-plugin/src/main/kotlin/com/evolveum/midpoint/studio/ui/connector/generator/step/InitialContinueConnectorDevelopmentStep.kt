/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.step

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.util.ui.JBFont

class InitialContinueConnectorDevelopmentStep(
    wizardContext : ConnectorGeneratorWizard,
    client : MidPointClient,
    state : GenerateConnectorBadge.State,
    isHeader : Boolean
) : ConnectorGeneratorGeneralWizardStep(wizardContext, client, state, isHeader) {

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Continue Connector Development"
        ) {
            panel {
                row {
                    cell(JBLabel("Connector Development continue").apply {
                        font = JBFont.label().deriveFont(16f)
                    })
                }

                row {
                    text("Initial Connector development step".trimIndent()).align(AlignX.FILL)
                }.bottomGap(BottomGap.MEDIUM)
            }
        }
    }

    override fun _init() {
        super._init()

        canGoNext = true
    }

    override fun _commit(finishChosen: Boolean) {
        state = GenerateConnectorBadge.State.COMPLETE
        super._commit(finishChosen)
    }
}