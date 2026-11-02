/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard

import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.ConnectorDevelopmentDataModel
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.wizard.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.step.InitialContinueConnectorDevelopmentStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.step.connection.AuthMethodSupportStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.step.connection.AuthScriptsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.step.connection.BaseUrlSpecificationStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.step.connection.CredentialsConnectorStep

/**
 * Wizard strategy for classic (non-SCIM) REST connectors. SCIM is a specialization of REST
 * ({@link ScimConnectorWizardStrategy} extends this).
 */
open class RestConnectorWizardStrategy: ConnectorWizardStrategy {

    override fun connectionSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {

        val connection = mutableListOf<ConnectorDevelopmentWizardStep>()

        connection.add(
            InitialContinueConnectorDevelopmentStep(
                wizardContext,
                client,
                Badge.State.IN_PROGRESS,
                false
            )
        )

        connection.add(
            BaseUrlSpecificationStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        connection.add(
            AuthMethodSupportStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        connection.add(
            AuthScriptsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        connection.add(
            CredentialsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        return connection
    }

    override fun initObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {
        TODO("Not yet implemented")
    }

    override fun searchAllObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {
        TODO("Not yet implemented")
    }

    override fun searchByIdObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {
        TODO("Not yet implemented")
    }

    override fun searchFilterObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {
        TODO("Not yet implemented")
    }

    override fun createObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {
        TODO("Not yet implemented")
    }

    override fun updateObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {
        TODO("Not yet implemented")
    }

    override fun deleteObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {
        TODO("Not yet implemented")
    }

    override fun connectionUrlFieldName(): ItemName {
        return BaseUrlSpecificationStep.BASE_ADDRESS_ITEM_NAME
    }
}