/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.ConnectorDevelopmentDataModel
import com.evolveum.midpoint.studio.ui.connector.development.util.ConnectorDevelopmentWizardUtil
import com.evolveum.midpoint.studio.ui.connector.development.util.TestingResourceState
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType

class ConnectorDevelopmentContinueWizard(
    private val client: MidPointClient,
    connectorDevelopmentType: ConnectorDevelopmentType
) : ConnectorDevelopmentWizard(client.project) {

    override val dataModel =
        ConnectorDevelopmentDataModel(
            connectorDevelopment = connectorDevelopmentType
        )

    init {
        helpButton.isVisible = false
        setSize(1300, 900)
        buildSteps()
        registerWizardSteps()
        resumeAtFirstIncompleteStep(readTestingResourceState())
        init()
    }

    /**
     * Null when the testing resource does not exist yet or cannot be read, the wizard then starts at the first step.
     */
    private fun readTestingResourceState(): TestingResourceState? =
        try {
            ConnectorDevelopmentWizardUtil.readTestingResourceState(client, dataModel.connectorDevelopment)
        } catch (e: Exception) {
            log.warn("Couldn't read the testing resource, the wizard starts at the first step", e)
            null
        }

    override fun buildSteps() {

        wizardStepsList.clear()

        val strategy = ConnectorDevelopmentWizardUtil.wizardStrategyFor(dataModel.connectorDevelopment)

        val connection = wizardStepsList.addCategory("Connection")
        strategy.connectionSteps(client, this, dataModel).forEach(connection::addStep)

        val objectClass = wizardStepsList.addCategory("Object class")
        strategy.initObjectClassSteps(client, this, dataModel).forEach(objectClass::addStep)

        // TODO per-operation step lists of the strategy (search all, search by id, search filter, create, update,
        //  delete) wait for server endpoints that save and validate generated scripts
    }

    override fun doOKAction() {
        super.doOKAction()
    }
}