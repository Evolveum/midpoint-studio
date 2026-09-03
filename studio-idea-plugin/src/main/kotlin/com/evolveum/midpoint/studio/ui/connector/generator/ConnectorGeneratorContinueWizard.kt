/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator

import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.dataModel.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.step.InitialContinueConnectorDevelopmentStep
import com.evolveum.midpoint.studio.ui.connector.generator.step.connection.AuthMethodSupportStep
import com.evolveum.midpoint.studio.ui.connector.generator.step.connection.AuthScriptsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.generator.step.connection.BaseUrlSpecificationStep
import com.evolveum.midpoint.studio.ui.connector.generator.step.connection.CredentialsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.generator.step.connection.TestConnectionStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType

class ConnectorGeneratorContinueWizard(
    private val client: MidPointClient,
    connectorDevelopmentType: ConnectorDevelopmentType
): ConnectorGeneratorWizard(client.project) {

    override val dataModel =
        ConnectorGeneratorDataModel(
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

        buildConnectionStepsList()
        buildObjectClassStepsList()
    }

    private fun buildConnectionStepsList() {

        val connection = wizardStepsList.addCategory("Connection")

        connection.addStep(
            InitialContinueConnectorDevelopmentStep(
                this,
                client,
                GenerateConnectorBadge.State.IN_PROGRESS,
                false
            )
        )

        connection.addStep(
            BaseUrlSpecificationStep(
                this,
                client,
                GenerateConnectorBadge.State.NONE,
                false
            )
        )

        connection.addStep(
            AuthMethodSupportStep(
                this,
                client,
                GenerateConnectorBadge.State.NONE,
                false
            )
        )

        connection.addStep(
            AuthScriptsConnectorStep(
                this,
                client,
                GenerateConnectorBadge.State.NONE,
                false
            )
        )

        connection.addStep(
            CredentialsConnectorStep(
                this,
                client,
                GenerateConnectorBadge.State.NONE,
                false
            )
        )

        connection.addStep(
            TestConnectionStep(
                this,
                client,
                GenerateConnectorBadge.State.NONE,
                false
            )
        )
    }

    private fun buildObjectClassStepsList() {

        val connection = wizardStepsList.addCategory("Object Class")

        connection.addStep(
            InitialContinueConnectorDevelopmentStep(
                this,
                client,
                GenerateConnectorBadge.State.NONE,
                false
            )
        )
    }

    override fun doOKAction() {
        super.doOKAction()
    }
}