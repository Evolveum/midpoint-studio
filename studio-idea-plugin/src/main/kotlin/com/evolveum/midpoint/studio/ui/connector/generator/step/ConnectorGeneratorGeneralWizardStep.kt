/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator.step

import com.evolveum.midpoint.prism.PrismObject
import com.evolveum.midpoint.studio.client.AuthenticationException
import com.evolveum.midpoint.studio.client.ClientException
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.impl.SearchOptions
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.util.exception.SchemaException
import com.evolveum.midpoint.xml.ns._public.common.common_3.*
import com.intellij.ide.wizard.CommitStepException
import com.intellij.ide.wizard.StepAdapter
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.Border

abstract class ConnectorGeneratorGeneralWizardStep(
    protected var wizardContext: ConnectorGeneratorWizard,
    protected var client: MidPointClient,
    protected var initialState: GenerateConnectorBadge.State,
    var isHeader: Boolean = false
): StepAdapter() {

    protected val log: Logger = Logger.getInstance(this.javaClass)
    protected abstract val dialogPanel: DialogPanel
    protected val dynamicPanel = JPanel(BorderLayout())
    protected val statusPanel = StatusPanel()
    protected val dataModel = wizardContext.dataModel
    protected var originalConnectorDevelopmentType: ConnectorDevelopmentType? = null

    open val nextButtonText: String
        get() = "Next"

    var state = initialState
        set(value) {
            field = value
            wizardContext.updateNavigationMenuByLiveStates()
        }

    var canGoNext = false
        set(value) {
            field = value
            wizardContext.updateWizardButtons()
        }

    override fun _init() {
        super._init()

        if (state == GenerateConnectorBadge.State.COMPLETE) {
            state = GenerateConnectorBadge.State.EDITED
        }

        if (state == GenerateConnectorBadge.State.NONE) {
            state = GenerateConnectorBadge.State.IN_PROGRESS
        }
    }

    open fun beforeProceedingToNextStep() {
    }

    override fun getComponent(): JComponent? {
        return if (state != GenerateConnectorBadge.State.NONE) {
            dialogPanel
        } else null
    }

    protected fun createDialogPanel(
        dialogTitle: String,
        content: Panel.() -> Unit
    ): DialogPanel =
        panel {
            content()
            row {
                cell(dynamicPanel)
                    .align(Align.FILL)
                    .resizableColumn()
            }
        }.apply {
            name = dialogTitle
        }

    @Throws(RuntimeException::class, SchemaException::class, AuthenticationException::class, IOException::class)
    protected fun upsertConnectorDevelopmentType(
        connDevConnectorType: ConnectorDevelopmentType
    ): ConnectorDevelopmentType {
        try {
            return client.upsert(connDevConnectorType.asPrismObject(), null)
        } catch (ex: ClientException) {
            throw CommitStepException(ex.result?.message ?: ex.message)
        } catch (ex: Exception) {
            throw CommitStepException("Couldn't update connector development object. \n Error: " + ex.message)
        }
    }

    protected fun continueFrom(
        oid: String
    ): ConnectorDevelopmentType {
        return client.continueFrom(oid)
    }

    protected fun submitOperation(
        submitOperationCall: () -> String,
        statusInfoCall: (String) -> SmartIntegrationOperationStatusInfoType?,
        project: Project,
        title: String,
        canBeCancelled: Boolean,
    ): CompletableFuture<SmartIntegrationOperationStatusInfoType> {

        val finalFuture = CompletableFuture<SmartIntegrationOperationStatusInfoType>()
        val scheduler = Executors.newSingleThreadScheduledExecutor()

        ProgressManager.getInstance().run(object: Backgroundable(
            project, title, canBeCancelled
        ) {
            override fun run(progressIndicator: ProgressIndicator) {
                try {
                    progressIndicator.text = "Submitting initialization operation - $title"

                    val retrievedToken = submitOperationCall()

                    if (retrievedToken.isBlank()) {
                        finalFuture.completeExceptionally(
                            IllegalStateException("Failed to obtain a valid tracking token from the server."))
                        return
                    }

                    var scheduledTask: ScheduledFuture<*>? = null

                    scheduledTask = scheduler.scheduleWithFixedDelay({
                        try {
                            if (progressIndicator.isCanceled) {
                                finalFuture.cancel(true)
                                scheduledTask?.cancel(false)
                                scheduler.shutdown()
                                return@scheduleWithFixedDelay
                            }

                            val statusInfo = statusInfoCall(retrievedToken)

                            if (statusInfo == null) {
                                finalFuture.completeExceptionally(
                                    NullPointerException("Received an empty status response payload."))
                                scheduledTask?.cancel(false)
                                scheduler.shutdown()
                            } else {
                                if (!statusInfo.status.equals(OperationResultStatusType.IN_PROGRESS)
                                        && !statusInfo.status.equals(OperationResultStatusType.UNKNOWN)
                                ) {
                                    finalFuture.complete(statusInfo)
                                    scheduledTask?.cancel(false)
                                    scheduler.shutdown()
                                }
                            }
                        } catch (e: Exception) {
                            finalFuture.completeExceptionally(e)
                            scheduledTask?.cancel(false)
                            scheduler.shutdown()
                        }
                    }, 0, 1, TimeUnit.SECONDS)

                    finalFuture.join()
                } catch (e: Exception) {
                    finalFuture.completeExceptionally(e)
                    scheduler.shutdown()
                } finally {
                    if (!scheduler.isShutdown) {
                        scheduler.shutdown()
                    }
                }
            }
        })

        return finalFuture
    }

    protected fun replaceContent(component: JComponent) {

        dynamicPanel.removeAll()
        dynamicPanel.add(component, BorderLayout.CENTER)
        dynamicPanel.revalidate()
        dynamicPanel.repaint()
    }

    protected fun getSelectedBorder(): Border? {
        return JBUI.Borders.compound(
            JBUI.Borders.customLine(JBColor(0x58A6FF, 0x2F81F7), 1),
            JBUI.Borders.empty(11, 15)
        )
    }

    protected fun getUnselectedBorder(): Border? {
        return JBUI.Borders.compound(
            JBUI.Borders.customLine(JBColor(0xE0E0E0, 0x3F4143), 1),
            JBUI.Borders.empty(12, 16)
        )
    }

    protected fun isScim(): Boolean? {
        return dataModel.connectorDevelopment.connector?.integrationType?.equals(ConnDevIntegrationType.SCIM)
    }

    protected fun getLoadingComponent(
        statusPanel: StatusPanel,
        title: String,
        description: String?
    ): JComponent = panel {
        row {
            val loadingComponent = statusPanel.showLoadingComponent(
                title,
                description,
                0
            )

            cell(loadingComponent)
                .align(Align.FILL)
        }.resizableRow()
    }

    protected fun getAlertComponent(
        statusPanel: StatusPanel,
        title: String,
        description: String?
    ): JComponent {

        val message = buildString {
            append(title)
            description?.let {
                append(": ")
                append(it)
            }
        }

        when (statusPanel.status) {
            StatusPanel.Status.ERROR -> log.error(message)
            StatusPanel.Status.WARNING -> log.warn(message)
            StatusPanel.Status.INFO -> log.info(message)
            StatusPanel.Status.SUCCESS -> log.info(message)
            else -> log.debug(message)
        }

        return panel {
            row {
                val alertComponent = statusPanel.showAlertComponent(
                    statusPanel.status,
                    title,
                    description,
                    JBColor(Gray._255, Gray._255)
                )

                cell(alertComponent)
                    .align(Align.FILL)
            }.resizableRow()
        }
    }

    protected fun existConnDev(): Boolean {

        dataModel.connectorDevelopment?.oid?: run {
            statusPanel.status = StatusPanel.Status.ERROR
            replaceContent(getAlertComponent(
                statusPanel,
                statusPanel.status?.name?: "",
                "Connector Development oid missing"
            ))

            return false
        }

        return true
    }

    @Throws(SchemaException::class, NullPointerException::class)
    protected fun <T : ObjectType> getObjectByOid(
        oid: String,
        type: Class<T>
    ): PrismObject<T> {

        if (oid.isBlank()) {
            throw NullPointerException("Object OID not available")
        }

        val searchOptions = SearchOptions().apply {
            raw(true)
        }

        val midpointObject = client.get(
            type,
            oid,
            searchOptions
        )

        return client.prismContext
            .parserFor(midpointObject.content)
            .parse()
    }

    protected fun hasChanges(originalDataModel: ConnectorDevelopmentType?): Boolean =
        originalDataModel != dataModel.connectorDevelopment

    protected fun String?.requireNotBlank(fieldName: String) {
        if (isNullOrBlank()) {
            throw CommitStepException("Field $fieldName is required")
        }
    }
}
