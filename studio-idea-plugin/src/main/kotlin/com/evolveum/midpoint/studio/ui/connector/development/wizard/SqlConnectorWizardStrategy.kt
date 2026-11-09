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
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.InitialContinueConnectorDevelopmentStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.connection.ResourceTestConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.ObjectClassSelectConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.create.CreateScriptConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.delete.DeleteScriptConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.schema.SchemaScriptConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.schema.ShowSchemaConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.search.SearchAllEndpointsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.search.SearchAllObjectsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.search.SearchAllScriptConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.search.SearchByIdEndpointsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.search.SearchByIdObjectConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.search.SearchByIdScriptConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.search.SearchFilterEndpointsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.search.SearchFilterObjectsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.search.SearchFilterScriptConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.update.UpdateScriptConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.sql.step.connection.SqlConnectionParametersConnectorStep

class SqlConnectorWizardStrategy: ConnectorWizardStrategy {

    private val NOT_APPLICABLE_TO_SQL = "This wizard step is HTTP-specific and does not apply to SQL connectors."

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
            ResourceTestConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        connection.add(
            SqlConnectionParametersConnectorStep(
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

        val objectClassSteps = mutableListOf<ConnectorDevelopmentWizardStep>()

        objectClassSteps.add(
            ObjectClassSelectConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        objectClassSteps.add(
            SchemaScriptConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        objectClassSteps.add(
            ShowSchemaConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        objectClassSteps.add(
            SearchAllEndpointsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        objectClassSteps.add(
            SearchAllScriptConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        objectClassSteps.add(
            SearchAllObjectsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        objectClassSteps.add(
            SearchByIdScriptConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        objectClassSteps.add(
            SearchByIdObjectConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        return objectClassSteps
    }

    override fun searchAllObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {

        val searchAllObjectClassSteps = mutableListOf<ConnectorDevelopmentWizardStep>()

        searchAllObjectClassSteps.add(
            SearchAllScriptConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        searchAllObjectClassSteps.add(
            SearchAllObjectsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        return searchAllObjectClassSteps
    }

    override fun searchByIdObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {

        val searchByIdObjectClassSteps = mutableListOf<ConnectorDevelopmentWizardStep>()

        searchByIdObjectClassSteps.add(
            SearchByIdEndpointsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        searchByIdObjectClassSteps.add(
            SearchByIdScriptConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        searchByIdObjectClassSteps.add(
            SearchByIdObjectConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        return searchByIdObjectClassSteps
    }

    override fun searchFilterObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {

        val searchFilterObjectClassSteps = mutableListOf<ConnectorDevelopmentWizardStep>()

        searchFilterObjectClassSteps.add(
            SearchFilterEndpointsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        searchFilterObjectClassSteps.add(
            SearchFilterScriptConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        searchFilterObjectClassSteps.add(
            SearchFilterObjectsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        return searchFilterObjectClassSteps
    }

    override fun createObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {

        val createObjectClassSteps = mutableListOf<ConnectorDevelopmentWizardStep>()

        createObjectClassSteps.add(
            CreateScriptConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        return createObjectClassSteps
    }

    override fun updateObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {

        val updateObjectClassSteps = mutableListOf<ConnectorDevelopmentWizardStep>()

        updateObjectClassSteps.add(
            UpdateScriptConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        return updateObjectClassSteps
    }

    override fun deleteObjectClassSteps(
        client: MidPointClient,
        wizardContext: ConnectorDevelopmentWizard,
        connectorDevelopmentDataModel: ConnectorDevelopmentDataModel
    ): List<ConnectorDevelopmentWizardStep> {
        val deleteObjectClassSteps = mutableListOf<ConnectorDevelopmentWizardStep>()

        deleteObjectClassSteps.add(
            DeleteScriptConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        return deleteObjectClassSteps
    }

    override fun connectionUrlFieldName(): ItemName {
        throw UnsupportedOperationException(NOT_APPLICABLE_TO_SQL)
    }
}