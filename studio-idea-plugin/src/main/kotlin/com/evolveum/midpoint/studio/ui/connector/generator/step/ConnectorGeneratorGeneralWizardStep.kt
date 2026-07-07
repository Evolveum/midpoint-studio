package com.evolveum.midpoint.studio.ui.connector.generator.step

import com.evolveum.midpoint.studio.client.AuthenticationException
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.component.StatusPanel
import com.evolveum.midpoint.util.exception.SchemaException
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractSmartIntegrationOperationResultType
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType
import com.evolveum.midpoint.xml.ns._public.common.common_3.SmartIntegrationOperationStatusInfoType
import com.intellij.ide.wizard.StepAdapter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import javax.swing.JPanel
import javax.swing.border.Border

open class ConnectorGeneratorGeneralWizardStep(
    protected var wizardContext: ConnectorGeneratorWizard,
    protected var client: MidPointClient,
    protected var dataModel: ConnectorGeneratorDataModel,
    private var state: GenerateConnectorBadge.State,
    private var isHeader: Boolean = false
) : StepAdapter() {

    protected val log: Logger = Logger.getInstance(this.javaClass)
    private var canGoNext = false

    fun getState(): GenerateConnectorBadge.State {
        return state
    }

    fun setState(state: GenerateConnectorBadge.State) {
        this.state = state
    }

    fun isHeader():Boolean {
        return isHeader
    }

    fun canGoNext(canGoNext: Boolean) {
        this.canGoNext = canGoNext
    }

    fun isCanGoNext(): Boolean {
        return canGoNext
    }

    @Throws(RuntimeException::class, SchemaException::class, AuthenticationException::class, IOException::class)
    protected fun upsertConnectorDevelopmentType(
        connDevConnectorType: ConnectorDevelopmentType
    ): ConnectorDevelopmentType? {
        return client.upsert(connDevConnectorType.asPrismObject(), null)
    }

    @Throws(CompletionException::class)
    protected fun getResult(
        statusInfoFetcher: Supplier<SmartIntegrationOperationStatusInfoType>
    ): CompletableFuture<AbstractSmartIntegrationOperationResultType?> {
        val future =
            CompletableFuture<AbstractSmartIntegrationOperationResultType?>()

        val scheduler = Executors.newSingleThreadScheduledExecutor()

        scheduler.scheduleWithFixedDelay({
            try {
                val statusInfo = statusInfoFetcher.get()

                if (statusInfo.status == OperationResultStatusType.UNKNOWN) {
                    return@scheduleWithFixedDelay
                }

                if (statusInfo.status != OperationResultStatusType.IN_PROGRESS) {
                    if (statusInfo.status == OperationResultStatusType.SUCCESS) {
                        future.complete(statusInfo.result)
                    } else {
                        future.completeExceptionally(
                            RuntimeException(
                                "Operation failed: " + statusInfo.status +
                                        (if (statusInfo.message != null) ", Message: " + statusInfo.message else "")
                            )
                        )
                    }
                    scheduler.shutdown()
                }
            } catch (e: Exception) {
                log.error(e)
                future.completeExceptionally(e)
                scheduler.shutdown()
            }
        }, 0, 1, TimeUnit.SECONDS)

        return future
    }

    protected fun printWaitingPanel(mainPanel: JPanel, statusPanel: StatusPanel, title: String, description: String?) {
        mainPanel.removeAll()
        mainPanel.add(panel {
            row {
                val loadingComponent = statusPanel.showLoadingPanel(
                    title,
                    description,
                    0
                )

                cell(loadingComponent)
                    .align(Align.FILL)
            }.resizableRow()
        })
        mainPanel.revalidate()
        mainPanel.repaint()
    }

    protected fun printAlertPanel(status: StatusPanel.Status, mainPanel: JPanel, statusPanel: StatusPanel, title: String, description: String?) {
        mainPanel.removeAll()
        mainPanel.add(panel {
            row {
                val alertComponent = statusPanel.showAlertPanel(
                    status,
                    title,
                    description,
                    JBColor(Gray._255, Gray._255)
                )

                cell(alertComponent)
                    .align(Align.FILL)
            }.resizableRow()
        })
        mainPanel.revalidate()
        mainPanel.repaint()
    }

    protected fun printTokenNullAlertPanel(mainPanel: JPanel, statusPanel: StatusPanel) {
        val errorMsg = "Token is Null"
        printAlertPanel(StatusPanel.Status.ERROR, mainPanel, statusPanel, "Error", errorMsg)
        log.error("Submit operation failed at the ${mainPanel.parent?.name} step. $errorMsg")
    }

    protected fun getSelectedBorder() : Border? {

        return JBUI.Borders.compound(
            JBUI.Borders.customLine(JBColor(0x58A6FF, 0x2F81F7), 1),
            JBUI.Borders.empty(11, 15)
        )
    }

    protected fun getUnselectedBorder() : Border? {

        return JBUI.Borders.compound(
            JBUI.Borders.customLine(JBColor(0xE0E0E0, 0x3F4143), 1),
            JBUI.Borders.empty(12, 16)
        )
    }
}