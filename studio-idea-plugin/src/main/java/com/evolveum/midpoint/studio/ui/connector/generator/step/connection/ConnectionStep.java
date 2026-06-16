package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.service.TaskStatusPoller;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDiscoverGlobalInformationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ConnectionStep extends ConnectorGeneratorGeneralWizardStep {

    private final TaskStatusPoller taskStatusPoller;
    private Connection connection;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public ConnectionStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        super(wizardContext, client, dataModel, state, isHeader);
        this.taskStatusPoller = ApplicationManager.getApplication().getService(TaskStatusPoller.class);
        mainPanel.setName("Connection");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;

            connection = new Connection(getDataModel());

            ProgressManager.getInstance().run(new Task.Backgroundable(getClient().getProject(),
                    "DiscoverBasicInformation submit operation",
                    true
            ) {
                private String token;

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        token = getClient().submitOperationDiscoverBasicInformation(
                                getDataModel().connectorDevelopmentType.getOid());
                    } catch (SchemaException | IOException | AuthenticationException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onSuccess() {
                    var statusPanel = connection.getStatusPanel();

                    if (token == null) {
                        printAlert(mainPanel, statusPanel, "Error", "Token is Null");
                        setState(GenerateConnectorBadge.State.FIXING);
                        return;
                    }

                    mainPanel.add(statusPanel.showLoadingPanel(
                            "Identifying Connection Possibilities...",
                            """
                                This involves exploring and determining the available options for establishing a connection, including supported protocols, authentication methods, and endpoints.
                                """,
                            0
                    ));

                    var timer = new Timer(1000, event -> {
                        statusPanel.updateElapsed(taskStatusPoller.getElapsedTime().toSeconds());

                        if (taskStatusPoller.getStatus() != null
                                && !OperationResultStatusType.IN_PROGRESS.equals(taskStatusPoller.getStatus())
                        ) {
                            if (OperationResultStatusType.SUCCESS.equals(taskStatusPoller.getStatus())) {
                                ProgressManager.getInstance().run(new Backgroundable(getClient().getProject(),
                                        "DiscoverBasicInformation get result",
                                        true
                                ) {
                                    private ConnDevDiscoverGlobalInformationResultType result;

                                    @Override
                                    public void run(@NotNull ProgressIndicator progressIndicator) {
                                        try {
                                            result = getClient().getResultDiscoverBasicInformation(token);
                                        } catch (Exception e) {
                                            printAlert(mainPanel, statusPanel, "Error", e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onSuccess() {
                                        super.onSuccess();

                                        mainPanel.removeAll();

                                        if (result != null) {
                                            getWizardContext().doNextAction();
                                            mainPanel.revalidate();
                                            mainPanel.repaint();
                                        } else {
                                            printAlert(mainPanel, statusPanel, OperationResultStatusType.UNKNOWN.name(), "Result Null");
                                        }
                                    }
                                });
                            } else {
                                ProgressManager.getInstance().run(new Backgroundable(getClient().getProject(),
                                        "DiscoverBasicInformation get message",
                                        true
                                ) {
                                    private String message;

                                    @Override
                                    public void run(@NotNull ProgressIndicator progressIndicator) {
                                        try {
                                            message = getClient().getMessageDiscoverBasicInformation(token);
                                        } catch (Exception ex) {
                                            printAlert(mainPanel, statusPanel, "Error", ex.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onFinished() {
                                        super.onFinished();
                                        printAlert(mainPanel, statusPanel, taskStatusPoller.getStatus().name(), message);
                                    }
                                });
                            }

                            taskStatusPoller.stopPolling();
                            ((Timer) event.getSource()).stop();
                        }
                    });

                    taskStatusPoller.startPolling(() -> {
                        try {
                            return getClient().getStatusDiscoverBasicInformation(token);
                        } catch (Exception e) {
                            taskStatusPoller.stopPolling();
                            timer.stop();
                            printAlert(mainPanel, statusPanel, "Error", e.getMessage());
                        }

                        return null;
                    });

                    timer.start();

                    super.onSuccess();
                }
            });
        }

        super._init();
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {

        if (getDataModel().connectorDevelopmentType == null) {
            throw new CommitStepException(
                    "Failed to update ConnectorDevelopmentType object"
            );
        } else {
            setState(GenerateConnectorBadge.State.COMPLETE);
            canGoNext(true);
        }

        super._commit(finishChosen);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
