/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.generator

import com.evolveum.midpoint.prism.Containerable
import com.evolveum.midpoint.prism.PrismContainer
import com.evolveum.midpoint.prism.PrismContainerDefinition
import com.evolveum.midpoint.prism.PrismObject
import com.evolveum.midpoint.studio.action.task.DownloadConnectorDevelopmentTask
import com.evolveum.midpoint.studio.impl.EncryptionService
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge
import com.evolveum.midpoint.studio.ui.connector.generator.dataModel.ConnectorGeneratorDataModel
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.ApplicationIdentificationStep
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.ConnectorIdentificationStep
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.CreateConnectorStep
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.DiscoverDocumentationStep
import com.evolveum.midpoint.studio.util.MidPointUtils
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.Messages

class ConnectorGeneratorBasicWizard(
    private val client: MidPointClient
): ConnectorGeneratorWizard(client.project) {

    override val dataModel: ConnectorGeneratorDataModel =
        ConnectorGeneratorDataModel(
            connectorDevelopment = initConnectorDevelopment().asObjectable().clone()
        )

    init {
        helpButton.isVisible = false
        setSize(1300, 900)
        buildSteps()
        registerWizardSteps()
        init()
    }

    override fun buildSteps() {

        wizardStepsList.clear()

        wizardStepsList.addStep(
            ApplicationIdentificationStep(
                this,
                client,
                GenerateConnectorBadge.State.IN_PROGRESS,
                false
            )
        )

        wizardStepsList.addStep(
            DiscoverDocumentationStep(
                this,
                client,
                GenerateConnectorBadge.State.NONE,
                false
            )
        )

        wizardStepsList.addStep(
            ConnectorIdentificationStep(
                this,
                client,
                GenerateConnectorBadge.State.NONE,
                false
            )
        )

        wizardStepsList.addStep(
            CreateConnectorStep(
                this,
                client,
                GenerateConnectorBadge.State.NONE,
                false
            )
        )
    }

    override fun doOKAction() {

        MidPointUtils.publishNotification(
            client.project,
            EncryptionService.NOTIFICATION_KEY,
            "Connector Generator",
            "Connector ${dataModel.connectorDevelopment.name} downloaded",
            NotificationType.INFORMATION
        )

        try {
            ProgressManager.getInstance().run(
                DownloadConnectorDevelopmentTask(
                    client.project,
                    client.environment,
                    dataModel.connectorDevelopment.name.orig.replace(":", ".")
                ) {
                    ApplicationManager.getApplication().invokeLater {
                        showInfoNotificationWithAction(
                            client,
                            dataModel.connectorDevelopment,
                            "Connector Generator",
                            "Connector downloaded"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showErrorDialog(
                    client.project,
                    e.message,
                    "Error Create Connector"
                )
            }
            log.error(e)
        }

        super.doOKAction()
    }


    fun showInfoNotificationWithAction(
        client: MidPointClient,
        connectorDevelopmentType: ConnectorDevelopmentType,
        title: String,
        content: String
    ) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("midpointConnectorGenerator")
            .createNotification(title, content, NotificationType.INFORMATION)

        notification.addAction(object : AnAction("Continue Development Connector Generator") {
            override fun actionPerformed(e: AnActionEvent) {
                ApplicationManager.getApplication()
                    .invokeLater(Runnable { ConnectorGeneratorContinueWizard(client, connectorDevelopmentType).show() })
                notification.expire()
            }
        })

        notification.notify(client.project)
    }

    private fun initConnectorDevelopment(): PrismObject<ConnectorDevelopmentType> {
        val prismObject =
            client.prismContext.createObject(ConnectorDevelopmentType::class.java)
        val objectDefinition = prismObject.definition

        initializeContainers(prismObject, objectDefinition)

        return prismObject
    }

    private fun initializeContainers(
        container: PrismContainer<*>,
        definition: PrismContainerDefinition<*>
    ) {
        definition.definitions
            .filterIsInstance<PrismContainerDefinition<*>>()
            .forEach { childDefinition ->

                if (childDefinition.isMultiValue) {
                    return@forEach
                }

                val childContainer: PrismContainer<Containerable> =
                    container.findOrCreateContainer(childDefinition.itemName)

                initializeContainers(
                    childContainer,
                    childDefinition
                )
            }
    }
}
