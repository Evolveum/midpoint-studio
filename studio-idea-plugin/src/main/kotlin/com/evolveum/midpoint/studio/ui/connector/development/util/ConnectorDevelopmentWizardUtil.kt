/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.util

import com.evolveum.midpoint.studio.action.task.DownloadConnectorDevelopmentTask
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ConnectorWizardStrategy
import com.evolveum.midpoint.studio.ui.connector.development.wizard.RestConnectorWizardStrategy
import com.evolveum.midpoint.studio.ui.connector.development.wizard.ScimConnectorWizardStrategy
import com.evolveum.midpoint.studio.ui.connector.development.wizard.SqlConnectorWizardStrategy
import com.evolveum.midpoint.util.exception.SchemaException
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import org.jetbrains.annotations.NotNull

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
}