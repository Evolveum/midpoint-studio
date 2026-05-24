package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.action.task.DownloadConnectorDevelopmentTask;
import com.evolveum.midpoint.studio.action.task.UnpackConnectorDevelopmentTask;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.service.TaskStatusPoller;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;

import javax.swing.*;
import java.io.*;

public class CreateConnectorStep extends StepAdapter {

    private final MidPointClient client;
    private final TaskStatusPoller taskStatusPoller;
    private final ConnectorGeneratorDataModel dataModel;
    private StepStateBadge.State state;
    private final JBPanel<?> panel = new JBPanel<>();

    private boolean initialized = false;

    public CreateConnectorStep(
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            StepStateBadge.State state
    ) {
        this.client = client;
        this.taskStatusPoller = client.getProject().getService(TaskStatusPoller.class);
        this.dataModel = dataModel;
        this.state = state;
        panel.setName("Creating Connector");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;

            var token = client.submitOperationCreateConnector(dataModel.connectorDevelopmentType.getOid());

            if (token != null) {
                taskStatusPoller.startPolling(() -> {
                    if (taskStatusPoller.getStatus().equals(OperationResultStatusType.SUCCESS)) {
                        taskStatusPoller.stopPolling();

                        try {
                            // FIXME maybe replace .run to .runProcessWithProgressSynchronously
                            ProgressManager.getInstance().run(new DownloadConnectorDevelopmentTask(
                                    client.getProject(),
                                    client.getEnvironment(),
                                    dataModel.connectorDevelopmentType.getName().getOrig(),
                                    dataModel.connectorDevelopmentType.getVersion(),
                                    file -> ProgressManager.getInstance().run(new UnpackConnectorDevelopmentTask(
                                            client.getProject(),
                                            new File(client.getProject().getBasePath() + "/connid-connectors")
                                    ))
                            ));
                        } catch (Exception e) {
                            ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(
                                    client.getProject(),
                                    e.getMessage(),
                                    "Error Create Connector"
                            ));
                        }
                    }

                    return client.getStatusConnectorGenerator(token);
                });
            } else {
                ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(
                        client.getProject(),
                        "Token is null.",
                        "Error Create Connector"
                ));
            }
        }

        super._init();
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {
        super._commit(finishChosen);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    public StepStateBadge.State getState() {
        return state;
    }

    public void setState(StepStateBadge.State state) {
        this.state = state;
    }
}
