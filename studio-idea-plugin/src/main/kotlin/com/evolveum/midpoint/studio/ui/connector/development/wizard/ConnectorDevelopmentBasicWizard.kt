/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.connector.development.wizard

import com.evolveum.midpoint.prism.Containerable
import com.evolveum.midpoint.prism.PrismContainer
import com.evolveum.midpoint.prism.PrismContainerDefinition
import com.evolveum.midpoint.prism.PrismObject
import com.evolveum.midpoint.studio.impl.MidPointClient
import com.evolveum.midpoint.studio.impl.StudioPrismContextService
import com.evolveum.midpoint.studio.ui.connector.development.component.Badge
import com.evolveum.midpoint.studio.ui.connector.development.ConnectorDevelopmentDataModel
import com.evolveum.midpoint.studio.ui.connector.development.util.ConnectorDevelopmentWizardUtil
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.basic.ApplicationIdentificationStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.basic.ConnectorIdentificationStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.basic.CreateConnectorStep
import com.evolveum.midpoint.studio.ui.connector.development.wizard.scimrest.step.basic.DiscoverDocumentationStep
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages

class ConnectorDevelopmentBasicWizard(
    private val client: MidPointClient
): ConnectorDevelopmentWizard(client.project) {

    override val dataModel: ConnectorDevelopmentDataModel =
        ConnectorDevelopmentDataModel(
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
                Badge.State.IN_PROGRESS,
                false
            )
        )

        wizardStepsList.addStep(
            DiscoverDocumentationStep(
                this,
                client,
                Badge.State.NONE,
                false
            )
        )

        wizardStepsList.addStep(
            ConnectorIdentificationStep(
                this,
                client,
                Badge.State.NONE,
                false
            )
        )

        wizardStepsList.addStep(
            CreateConnectorStep(
                this,
                client,
                Badge.State.NONE,
                false
            )
        )
    }

    override fun doOKAction() {

        try {

            client.upsert(dataModel.connectorDevelopment.asPrismObject(), null)

            ConnectorDevelopmentWizardUtil.downloadConnectorDevelopment(
                client,
                dataModel.connectorDevelopment,
                Runnable {
                    showInfoNotificationWithAction()
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

    private fun showInfoNotificationWithAction() {

        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("midpointConnectorGenerator")
            .createNotification(
                "MidPoint Connector development",
                "Connector development downloaded",
                NotificationType.INFORMATION
            )

        notification.addAction(object : AnAction("Continue Development Connector Generator") {
            override fun actionPerformed(e: AnActionEvent) {
                ApplicationManager.getApplication()
                    .invokeLater(Runnable {
                        StudioPrismContextService.runWithProject(client.project) {
                            ConnectorDevelopmentContinueWizard(
                                client,
                                dataModel.connectorDevelopment
                            ).show()
                        }
                    })
                notification.expire()
            }
        })

        notification.notify(client.project)
    }
}
