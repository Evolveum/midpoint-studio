package com.evolveum.midpoint.studio.ui.connector.generator;

import com.evolveum.midpoint.studio.action.task.DownloadConnectorDevelopmentTask;
import com.evolveum.midpoint.studio.impl.EncryptionService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.ApplicationIdentificationStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.ConnectorIdentificationStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.CreateConnectorStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.DiscoverDocumentationStep;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

public class ConnectorGeneratorBasicWizard extends ConnectorGeneratorWizard {

    private final MidPointClient client;
    private final ConnectorGeneratorDataModel dataModel = new ConnectorGeneratorDataModel();

    public ConnectorGeneratorBasicWizard(@NotNull MidPointClient client) {
        super(client.getProject());
        this.client = client;
        getHelpButton().setVisible(false);
        setSize(1300, 600);
        buildSteps();
        init();
    }

    @Override
    protected void buildSteps() {
        myWizardStepsList.clear();
        myWizardStepsList.add(new ApplicationIdentificationStep(this, client, dataModel, GenerateConnectorBadge.State.IN_PROGRESS, false));
        myWizardStepsList.add(new DiscoverDocumentationStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
        myWizardStepsList.add(new ConnectorIdentificationStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
        myWizardStepsList.add(new CreateConnectorStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));

        myWizardStepsList.forEach(this::addStep);
        stepNavigationItems = new JBList<>(visibleListModel);

        updateNavigationMenuByLiveStates();
    }

    @Override
    protected void doOKAction() {

        MidPointUtils.publishNotification(client.getProject(), EncryptionService.NOTIFICATION_KEY, "Connector Generator",
                "Connector %s downloaded", NotificationType.INFORMATION);


        try {
            ProgressManager.getInstance().run(new DownloadConnectorDevelopmentTask(
                client.getProject(),
                client.getEnvironment(),
                dataModel.connectorDevelopmentType.getName().getOrig().replace(":" , "."),
                file -> ApplicationManager.getApplication().invokeLater(() -> showInfoNotificationWithAction(
                        client,
                        dataModel.connectorDevelopmentType,
                        "Connector Generator",
                        "Connector downloaded"
                )
            )));
        } catch (Exception e) {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(
                    client.getProject(), e.getMessage(), "Error Create Connector"));

            log.error(e);
        }

        super.doOKAction();
    }

    public void showInfoNotificationWithAction(
            @NotNull MidPointClient client,
            @NotNull ConnectorDevelopmentType connectorDevelopmentType,
            String title,
            String content
    ) {
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("midpointConnectorGenerator")
                .createNotification(title, content, NotificationType.INFORMATION);

        var oid = connectorDevelopmentType.getOid();

        if (oid != null) {
            notification.addAction(new AnAction("Continue Development Connector Generator") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    ApplicationManager.getApplication().invokeLater(() ->
                            new ConnectorGeneratorContinueWizard(client, "").show());
                    notification.expire();
                }
            });
        } else {
            notification.setContent("Connector Development Type oid is null");
            log.error(notification.getActions().toString());
        }

        notification.notify(client.getProject());
    }
}
