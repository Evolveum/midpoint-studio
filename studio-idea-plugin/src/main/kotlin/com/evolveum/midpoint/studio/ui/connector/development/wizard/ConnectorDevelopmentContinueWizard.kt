/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.ConnectorDevelopmentDataModel
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.util.ConnectorDevelopmentWizardUtil
import com.evolveum.midpoint.studio.ui.connector.development.wizard.step.ConnectorDevelopmentDisabledStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType

class ConnectorDevelopmentContinueWizard(
    private val client: MidPointClient,
    connectorDevelopmentType: ConnectorDevelopmentType
): ConnectorDevelopmentWizard(client.project) {

    override val dataModel =
        ConnectorDevelopmentDataModel(
            connectorDevelopment = connectorDevelopmentType
        )

    init {
        helpButton.isVisible = false
        setSize(1300, 900)
        buildSteps()
        registerWizardSteps()
        init()
    }

    override fun buildSteps() {

        wizardStepsList.clear()

        val strategy = ConnectorDevelopmentWizardUtil.wizardStrategyFor(dataModel.connectorDevelopment)

        wizardStepsList.addCategory("Connection")

        sequenceOf(
            strategy.connectionSteps(client, this,dataModel),
//            strategy.initObjectClassSteps(dataModel),
//            strategy.searchAllObjectClassSteps(dataModel),
//            strategy.searchByIdObjectClassSteps(dataModel),
//            strategy.searchFilterObjectClassSteps(dataModel),
//            strategy.createObjectClassSteps(dataModel),
//            strategy.updateObjectClassSteps(dataModel),
//            strategy.deleteObjectClassSteps(dataModel)
        ).flatten().forEach(wizardStepsList::addStep)
    }

    override fun doOKAction() {
        super.doOKAction()
    }
}