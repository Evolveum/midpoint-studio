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
import com.intellij.openapi.diagnostic.thisLogger
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
import org.jetbrains.annotations.NotNull
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
        }.also {
            it.name = dialogTitle
        }

    @Throws(RuntimeException::class, SchemaException::class, AuthenticationException::class, IOException::class)
    protected fun upsertConnectorDevelopmentType(
        @NotNull connDevConnectorType: ConnectorDevelopmentType
    ): ConnectorDevelopmentType {
        try {
            return client.upsert(connDevConnectorType.asPrismObject(), null)
        } catch (ex: ClientException) {
            throw CommitStepException(ex.result?.message ?: ex.message)
        } catch (ex: Exception) {
            throw CommitStepException("Couldn't update connector development object. \n Error: " + ex.message)
        }
    }

    protected fun startFromExistingConnectorDevelopment(
        oid: String
    ): ConnectorDevelopmentType {
        return client.startFromExisting(oid)
    }

    @Throws(IOException::class, SchemaException::class, AuthenticationException::class)
    protected fun continueFrom(): ConnectorDevelopmentType {
        val oid = dataModel.connectorDevelopment.oid
        val updated = client.continueFrom(oid)

        dataModel.connectorDevelopment = updated
        return updated
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
                log.info("Starting initialization operation: title='$title'")
                progressIndicator.text = "Submitting initialization operation - $title"

                try {
                    log.info("Submitting initialization operation: title='$title'")

                    val retrievedToken = submitOperationCall()

                    if (retrievedToken.isBlank()) {
                        log.error("Failed to obtain a valid tracking token from the server for operation: title='$title'")

                        finalFuture.completeExceptionally(
                            IllegalStateException("Failed to obtain a valid tracking token from the server."))
                        return
                    }

                    log.info("Successfully obtained tracking token for operation: title='$title'")

                    var scheduledTask: ScheduledFuture<*>? = null

                    scheduledTask = scheduler.scheduleWithFixedDelay({
                        try {
                            if (progressIndicator.isCanceled) {
                                log.info("Initialization operation was canceled: title='$title'")

                                finalFuture.cancel(true)
                                scheduledTask?.cancel(false)
                                scheduler.shutdown()
                                return@scheduleWithFixedDelay
                            }

                            log.info("Checking operation status: title='$title'")

                            val statusInfo = statusInfoCall(retrievedToken)

                            if (statusInfo == null) {
                                log.error("Received an empty status response payload: title='$title'")

                                finalFuture.completeExceptionally(
                                    NullPointerException("Received an empty status response payload."))
                                scheduledTask?.cancel(false)
                                scheduler.shutdown()
                            } else {
                                log.info("Received operation status: title='$title', status='${statusInfo.status}'")

                                if (statusInfo.status != OperationResultStatusType.IN_PROGRESS &&
                                    statusInfo.status != OperationResultStatusType.UNKNOWN
                                ) {
                                    log.info("Initialization operation completed: " + "title='$title', status='${statusInfo.status}'")

                                    finalFuture.complete(statusInfo)
                                    scheduledTask?.cancel(false)
                                    scheduler.shutdown()
                                }
                            }
                        } catch (e: Exception) {
                            log.error(e)
                            finalFuture.completeExceptionally(e)
                            scheduledTask?.cancel(false)
                            scheduler.shutdown()
                        }
                    }, 0, 1, TimeUnit.SECONDS)

                    log.info("Scheduled operation status polling: title='$title', interval='1 second'")

                    finalFuture.join()

                    log.info("Initialization operation finished successfully: title='$title'")
                } catch (e: Exception) {
                    log.error(e)
                    finalFuture.completeExceptionally(e)
                    scheduler.shutdown()
                } finally {
                    if (!scheduler.isShutdown) {
                        log.info("Shutting down scheduler: title='$title'")
                        scheduler.shutdown()
                    }

                    log.info("Initialization operation ended: title='$title'")
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

    protected fun getExceptionMessage(throwable: Throwable): String {
        val cause = throwable.cause ?: throwable
        return cause.message ?: "Unknown error"
    }
}
