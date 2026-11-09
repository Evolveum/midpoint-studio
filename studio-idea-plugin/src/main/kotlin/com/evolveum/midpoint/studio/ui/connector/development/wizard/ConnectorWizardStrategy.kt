/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard

import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.ConnectorDevelopmentDataModel
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep

/**
 * Connector-type-specific decisions used by the connector development wizard (REST, SCIM, SQL),
 * so step panels delegate here instead of branching on the connector's integration type themselves.
 */
interface ConnectorWizardStrategy {

    fun connectionSteps(client: MidPointClient, wizardContext: ConnectorDevelopmentWizard, connectorDevelopmentDataModel: ConnectorDevelopmentDataModel): List<ConnectorDevelopmentWizardStep>

    fun initObjectClassSteps(client: MidPointClient, wizardContext: ConnectorDevelopmentWizard, connectorDevelopmentDataModel: ConnectorDevelopmentDataModel): List<ConnectorDevelopmentWizardStep>

    fun searchAllObjectClassSteps(client: MidPointClient, wizardContext: ConnectorDevelopmentWizard, connectorDevelopmentDataModel: ConnectorDevelopmentDataModel): List<ConnectorDevelopmentWizardStep>

    fun searchByIdObjectClassSteps(client: MidPointClient, wizardContext: ConnectorDevelopmentWizard, connectorDevelopmentDataModel: ConnectorDevelopmentDataModel): List<ConnectorDevelopmentWizardStep>

    fun searchFilterObjectClassSteps(client: MidPointClient, wizardContext: ConnectorDevelopmentWizard, connectorDevelopmentDataModel: ConnectorDevelopmentDataModel): List<ConnectorDevelopmentWizardStep>

    fun createObjectClassSteps(client: MidPointClient, wizardContext: ConnectorDevelopmentWizard, connectorDevelopmentDataModel: ConnectorDevelopmentDataModel): List<ConnectorDevelopmentWizardStep>

    fun updateObjectClassSteps(client: MidPointClient, wizardContext: ConnectorDevelopmentWizard, connectorDevelopmentDataModel: ConnectorDevelopmentDataModel): List<ConnectorDevelopmentWizardStep>

    fun deleteObjectClassSteps(client: MidPointClient, wizardContext: ConnectorDevelopmentWizard, connectorDevelopmentDataModel: ConnectorDevelopmentDataModel): List<ConnectorDevelopmentWizardStep>

    /** Config property holding the connection base URL for this connector type. */
    fun connectionUrlFieldName(): ItemName
}