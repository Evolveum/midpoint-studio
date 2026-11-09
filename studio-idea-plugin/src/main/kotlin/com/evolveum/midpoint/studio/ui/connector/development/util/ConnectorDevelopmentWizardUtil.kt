/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.util

import com.evolveum.midpoint.prism.PrismContainerDefinition
import com.evolveum.midpoint.prism.PrismContext
import com.evolveum.midpoint.schema.constants.SchemaConstants
import com.evolveum.midpoint.schema.processor.ConnectorSchema
import com.evolveum.midpoint.schema.util.ConnectorTypeUtil
import com.evolveum.midpoint.studio.action.task.DownloadConnectorDevelopmentTask
import com.evolveum.midpoint.studio.client.AuthenticationException
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.impl.SearchOptions
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorWizardStrategy
import com.evolveum.midpoint.studio.ui.connector.development.wizard.RestConnectorWizardStrategy
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ScimConnectorWizardStrategy
import com.evolveum.midpoint.studio.ui.connector.development.wizard.SqlConnectorWizardStrategy
import com.evolveum.midpoint.util.exception.SchemaException
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import org.jetbrains.annotations.NotNull
import java.io.IOException

object ConnectorDevelopmentWizardUtil {

    private val log: Logger = Logger.getInstance(this.javaClass)

    /**
     * Whether the connector-generation service is not configured (offline mode) - in that case the
     * wizard runs with limited functionality: documentation discovery is skipped and scripts are
     * generated as skeletons the user edits manually.
     */
    @JvmStatic
    fun isOffline(@NotNull client: MidPointClient): Boolean {
        return client.isOfflineConnectorDevelopmentService
    }

    @JvmStatic
    fun isScim(@NotNull connectorDevelopment: ConnectorDevelopmentType): Boolean {
        return try {
            ConnDevIntegrationType.SCIM == connectorDevelopment.connector?.integrationType
        } catch (e: SchemaException) {
            log.info(e)
            false
        }
    }

    @JvmStatic
    fun isSql(@NotNull connectorDevelopment: ConnectorDevelopmentType): Boolean {
        return try {
            (connectorDevelopment.connector?.integrationType
                ?: connectorDevelopment.application?.integrationType) == ConnDevIntegrationType.SQL
        } catch (e: SchemaException) {
            log.info(e)
            return false
        }
    }

    /**
     * Resolves the [ConnectorWizardStrategy] for this connector's integration type, so wizard
     * step panels can delegate connector-type-specific decisions instead of branching themselves.
     */
    @JvmStatic
    fun wizardStrategyFor(@NotNull connectorDevelopment: ConnectorDevelopmentType): ConnectorWizardStrategy {
        if (isSql(connectorDevelopment)) {
            return SqlConnectorWizardStrategy()
        }
        if (isScim(connectorDevelopment)) {
            return ScimConnectorWizardStrategy()
        }
        return RestConnectorWizardStrategy()
    }

    /**
     * Downloads connector development from the MidPoint server to local storage
     * and invokes the specified callback after the download completes.
     *
     * The download is executed as an IntelliJ background task. The connector name
     * is derived from [ConnectorDevelopmentType.name] by replacing colons (`:`)
     * with dots (`.`).
     *
     * The [onSuccess] callback is scheduled on the IntelliJ event dispatch thread
     * using [ApplicationManager.getApplication] and [Application.invokeLater].
     *
     * @param client the MidPoint client providing the project and environment context required for the download.
     * @param connectorDevelopment the connector development to download from the MidPoint server.
     * @param onSuccess the callback to execute after the download completes.
     */
    @JvmStatic
    fun downloadConnectorDevelopment(
        @NotNull client: MidPointClient,
        @NotNull connectorDevelopment: ConnectorDevelopmentType,
        @NotNull onSuccess: Runnable
    ) {
        ProgressManager.getInstance().run(
            DownloadConnectorDevelopmentTask(
                client.project,
                client.environment,
                connectorDevelopment.name.orig.replace(":", ".")
            ) {
                ApplicationManager.getApplication().invokeLater {
                    onSuccess.run()
                }
            }
        )
    }

    /**
     * Creates a testing resource for the specified connector development.
     *
     * The resource is initialized with a name derived from the connector development
     * name and a reference to the specified connector. The resource is then persisted
     * using the provided [MidPointClient].
     *
     * @param client MidPoint client used to create and persist the resource.
     * @param connectorDevelopment Connector development containing the information  used to initialize
     * the testing resource.
     * @param connectorRef Reference to the connector associated with the testing resource.
     * @return The created or updated [ResourceType], or `null` if the client's [PrismContext] is not available.
     */
    @JvmStatic
    @Throws(SchemaException::class, AuthenticationException::class, IOException::class)
    fun createTestingResource(
        @NotNull client: MidPointClient,
        @NotNull connectorDevelopment: ConnectorDevelopmentType,
        @NotNull connectorRef: ObjectReferenceType
    ): ResourceType? {
        val prismContext: PrismContext = client.prismContext ?: return null
        val resourceObject = prismContext.createObject(ResourceType::class.java)
        val resourceType = resourceObject.asObjectable()
        resourceType.name = PolyStringType("Resource - " + connectorDevelopment.name?.orig)
        resourceType.connectorRef = connectorRef.clone()

        return client.upsert(resourceType.asPrismObject(), null)
    }

    /**
     * Retrieves the testing resource associated with the specified connector development.
     *
     * The resource is retrieved using the OID of the testing resource reference
     * defined in the testing configuration.
     *
     * @param client MidPoint client used to retrieve the testing resource.
     * @param connectorDevelopment Connector development containing the testing resource reference.
     * @return The retrieved [ResourceType].
     */
    @JvmStatic
    @Throws(IOException::class, SchemaException::class, AuthenticationException::class)
    fun getTestingResource(
        client: MidPointClient,
        connectorDevelopment: ConnectorDevelopmentType
    ): ResourceType =
        client.parseObject(
            client.get(
                ResourceType::class.java,
                connectorDevelopment.testing?.testingResource?.oid,
                SearchOptions()
            ).content
        )?.asObjectable() as ResourceType

    /**
     * Retrieves the connector type associated with the specified connector development.
     *
     * The connector type is retrieved using the OID from the connector reference
     * defined in the connector development.
     *
     * @param client MidPoint client used to retrieve the connector type.
     * @param connectorDevelopment Connector development containing the connector reference.
     * @return The retrieved [ConnectorType].
     */
    @JvmStatic
    @Throws(IOException::class, SchemaException::class, AuthenticationException::class)
    fun getConnectorType(
        client: MidPointClient,
        connectorDevelopment: ConnectorDevelopmentType
    ): ConnectorType =
        client.parseObject(
            client.get(
                ConnectorType::class.java,
                connectorDevelopment.connector?.connectorRef?.oid,
                SearchOptions()
            )?.content
        ).asObjectable() as ConnectorType

    /**
     * Finds the definition of the connector configuration properties container.
     *
     * Parses the connector schema, if available, and searches for the configuration properties container definition.
     *
     * @param connectorType Connector type whose schema is used to find the definition.
     * @return The configuration properties container definition, or `null` if the connector schema is unavailable or
     * the definition cannot be found.
     */
    @JvmStatic
    @Throws(SchemaException::class)
    fun findConnectorConfigurationPropertiesDefinition(
        connectorType: ConnectorType
    ): PrismContainerDefinition<*>? =
        ConnectorTypeUtil.parseConnectorSchemaIfPresent(connectorType)
            ?.connectorConfigurationContainerDefinition
            ?.findLocalItemDefinition(SchemaConstants.ICF_CONFIGURATION_PROPERTIES_NAME)
}