/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard.sql.step.connection

import com.evolveum.midpoint.prism.path.ItemName
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.impl.StudioPrismContextService
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.component.StatusPanel
import com.evolveum.midpoint.studio.ui.connector.development.util.ConnectorDevelopmentWizardUtil
import com.evolveum.midpoint.studio.ui.connector.development.util.TestingResourceState
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorDevelopmentWizard
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.ConnectorDevelopmentWizardStep
import com.evolveum.midpoint.studio.ui.prism.PrismDefinitionPanel
import com.evolveum.midpoint.studio.ui.prism.PrismDefinitionPanelBuilder
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.util.ui.JBFont

/**
 * JDBC connection parameters of the SQL testing resource. The form is rendered from the connector configuration
 * schema, the same way the credentials step renders the HTTP authentication properties.
 */
class SqlConnectionParametersConnectorStep(
    wizardContext : ConnectorDevelopmentWizard,
    client : MidPointClient,
    state : Badge.State,
    isHeader : Boolean
): ConnectorDevelopmentWizardStep(wizardContext, client, state, isHeader) {

    private val builder = PrismDefinitionPanelBuilder()

    private var parametersPanel: PrismDefinitionPanel? = null
    private var savedValues: Map<String, Any?> = emptyMap()

    override val dialogPanel: DialogPanel by lazy {
        createDialogPanel(
            "Connection parameters"
        ) {
            row {
                cell(JBLabel("Set Database Connection Parameters").apply {
                    font = JBFont.label().deriveFont(16f)
                })
            }

            row {
                text("""
                Enter the JDBC URL, username and password used to connect to the target database, plus any optional connection pool or validation settings you need.
                """.trimIndent()).align(AlignX.FILL)
            }.bottomGap(BottomGap.MEDIUM)

            separator()
        }
    }

    override fun isCompleted(resource: TestingResourceState?): Boolean =
        resource?.hasAll(REQUIRED.keys) == true

    override fun _init() {
        super._init()

        if (!existConnDev()) return

        originalConnectorDevelopmentType = dataModel.connectorDevelopment.clone()

        // the step is initialized on every visit, the rendered fields keep what the user typed
        if (parametersPanel != null) {
            canGoNext = true
            return
        }

        try {
            val connector = ConnectorDevelopmentWizardUtil.getConnectorType(client, dataModel.connectorDevelopment)
            val definition = ConnectorDevelopmentWizardUtil.findConnectorConfigurationPropertiesDefinition(connector)
                ?: throw IllegalStateException("The connector has no configuration schema yet.")

            savedValues = ConnectorDevelopmentWizardUtil.readTestingResourceState(
                client,
                dataModel.connectorDevelopment
            ).configuration

            val rendered = builder.build(FIELDS, definition, savedValues)
            parametersPanel = rendered

            replaceContent(rendered.panel)
            canGoNext = true
        } catch (e: Exception) {
            log.error("Couldn't render SQL connection parameters", e)
            statusPanel.status = StatusPanel.Status.ERROR
            replaceContent(
                getAlertComponent(
                    statusPanel,
                    statusPanel.status?.name ?: "Error",
                    getExceptionMessage(e)
                )
            )
        }
    }

    override fun _commit(finishChosen: Boolean) {
        super._commit(finishChosen)

        val panel = parametersPanel
            ?: throw CommitStepException("Connection parameters are not available.")

        val values = valuesToSave(panel.values().mapKeys { it.key.localPart })

        REQUIRED.forEach { (name, label) ->
            if (values[name] == null && savedValues[name] == null) {
                throw CommitStepException("Field $label is required")
            }
        }

        try {
            StudioPrismContextService.runWithProject(client.project) {
                ConnectorDevelopmentWizardUtil.updateTestingResourceConfiguration(
                    client,
                    dataModel.connectorDevelopment,
                    values,
                    markDown = true
                )
            }
        } catch (e: Exception) {
            log.error("Couldn't save SQL connection parameters", e)
            throw CommitStepException("Couldn't save the connection parameters. \n Error: " + e.message)
        }

        if (state == Badge.State.IN_PROGRESS ||
            state == Badge.State.EDITED
        ) {
            dataModel.occurredChanges = hasChanges(originalConnectorDevelopmentType)
            updateConnectorDevelopment()
            state = Badge.State.COMPLETE
        }
    }

    /**
     * The server returns protected values encrypted, so the password field starts empty on a revisit. An empty
     * password keeps the value stored on the resource instead of removing it.
     */
    private fun valuesToSave(entered: Map<String, Any?>): Map<String, Any?> =
        entered.filterNot { (name, value) -> value == null && savedValues[name] is ProtectedStringType }

    companion object {
        private const val JDBC_URL = "jdbcUrl"
        private const val USERNAME = "username"
        private const val PASSWORD = "password"

        private val REQUIRED = mapOf(
            JDBC_URL to "JDBC Database URL",
            USERNAME to "Username",
            PASSWORD to "Password"
        )

        /** Same fields as the web GUI shows, names missing in the connector schema are skipped by the builder. */
        private val FIELDS = listOf(
            JDBC_URL,
            USERNAME,
            PASSWORD,
            "poolSize",
            "connectionTimeout",
            "idleTimeout",
            "validateConnectionOnBorrow",
            "autoDiscoverSchema",
            "testConnectionQuery"
        ).map { ItemName.from("", it) }
    }
}
