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
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.connection.AuthMethodSupportStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.connection.AuthScriptsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.connection.BaseUrlSpecificationStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.connection.CredentialsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.ObjectClassSelectConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.create.CreateEndpointsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.create.CreateScriptConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.delete.DeleteEndpointsConnectorStep
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
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.update.UpdateEndpointsConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.objectclass.update.UpdateScriptConnectorStep

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

        val connectionSteps = mutableListOf<ConnectorDevelopmentWizardStep>()

        connectionSteps.add(
            InitialContinueConnectorDevelopmentStep(
                wizardContext,
                client,
                Badge.State.IN_PROGRESS,
                false
            )
        )

        connectionSteps.add(
            BaseUrlSpecificationStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        connectionSteps.add(
            AuthMethodSupportStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        connectionSteps.add(
            AuthScriptsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        connectionSteps.add(
            CredentialsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

        return connectionSteps
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
            SearchByIdEndpointsConnectorStep(
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
            SearchAllEndpointsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

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
            CreateEndpointsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

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
            UpdateEndpointsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

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
            DeleteEndpointsConnectorStep(
                wizardContext,
                client,
                Badge.State.NONE,
                false
            )
        )

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
        return BaseUrlSpecificationStep.BASE_ADDRESS_ITEM_NAME
    }
}