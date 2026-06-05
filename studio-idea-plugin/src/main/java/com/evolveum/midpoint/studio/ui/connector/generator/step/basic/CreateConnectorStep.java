package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.service.TaskStatusPoller;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.other.StatusPanel;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevCreateConnectorResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class CreateConnectorStep extends ConnectorGeneratorWizardStep {

    private final MidPointClient client;
    private final TaskStatusPoller taskStatusPoller;
    private final ConnectorGeneratorDataModel dataModel;
    private CreateConnector createConnector;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    private boolean initialized = false;

    public CreateConnectorStep(
            ConnectorGeneratorBasicWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            StepStateBadge.State state
    ) {
        super(wizardContext, state);
        this.client = client;
        this.taskStatusPoller = ApplicationManager.getApplication().getService(TaskStatusPoller.class);
        this.dataModel = dataModel;
        mainPanel.setName("Creating Connector");
    }

    @Override
    public void _init() {
        if (!initialized) {
            initialized = true;

            setState(StepStateBadge.State.IN_PROGRESS);

            createConnector = new CreateConnector();

            ProgressManager.getInstance().run(new Task.Backgroundable(client.getProject(),
                    "CreateConnector submit operation",
                    true
            ) {

                private String token;

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        token = client.submitOperationCreateConnector(dataModel.connectorDevelopmentType.getOid());
                    } catch (SchemaException | IOException | AuthenticationException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onSuccess() {
                    var statusPanel = createConnector.getStatusPanel();

                    if (token == null) {
                        ApplicationManager.getApplication().invokeLater(
                                () -> printAlert(statusPanel, "Error", "Token is Null"));
                        return;
                    }

                    ApplicationManager.getApplication().invokeLater(() -> mainPanel.add(statusPanel.showLoadingPanel(
                            "Creating Connector...",
                            "We use the connector's basic information to create a test instance for development and testing purposes.",
                            0
                    )));

                    var timer = new Timer(1000, event -> {
                        statusPanel.updateElapsed(taskStatusPoller.getElapsedTime().toSeconds());

                        if (taskStatusPoller.getStatus() != null
                                && !OperationResultStatusType.IN_PROGRESS.equals(taskStatusPoller.getStatus())
                        ) {
                            if (OperationResultStatusType.SUCCESS.equals(taskStatusPoller.getStatus())) {
                                ProgressManager.getInstance().run(new Backgroundable(client.getProject(),
                                        "CreateConnector get result",
                                        true
                                ) {
                                    private ConnDevCreateConnectorResultType result;

                                    @Override
                                    public void run(@NotNull ProgressIndicator progressIndicator) {
                                        try {
                                            result = client.getResultCreateConnector(token);
                                        } catch (Exception e) {
                                            printAlert(statusPanel, "Error", e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onSuccess() {
                                        super.onSuccess();

                                        ApplicationManager.getApplication().invokeLater(() -> {
                                            if (result != null) {
                                                if (result.getConnectorRef() != null) {
                                                    try {
                                                        var prismContext = client.getPrismContext();
                                                        PrismObject<ResourceType> resourceObject = prismContext.createObject(ResourceType.class);
                                                        ResourceType resourceType = resourceObject.asObjectable();
                                                        resourceType.setName(dataModel.connectorDevelopmentType.getName());
                                                        ObjectReferenceType clonedRef = result.getConnectorRef().clone();
                                                        resourceType.setConnectorRef(clonedRef);
                                                        client.upload(resourceType.asPrismObject(), null);
                                                    } catch (SchemaException | AuthenticationException | IOException e) {
                                                        throw new RuntimeException(e);
                                                    }

                                                    printAlert(statusPanel,
                                                        OperationResultStatusType.SUCCESS.name(),
                                                        "Successfully Generated Connector Development"
                                                    );

                                                    canGoNext(true);
                                                    getWizardBasicContext().updateWizardButtons();
                                                    setState(StepStateBadge.State.COMPLETE);
                                                } else {
                                                    printAlert(statusPanel,
                                                            OperationResultStatusType.UNKNOWN.name(),
                                                            "ConnectorRef Null"
                                                    );
                                                }
                                            } else {
                                                printAlert(statusPanel,
                                                        OperationResultStatusType.UNKNOWN.name(),
                                                        "Result Null"
                                                );
                                            }
                                        });
                                    }
                                });
                            } else {
                                ProgressManager.getInstance().run(new Backgroundable(client.getProject(),
                                        "CreateConnector get message",
                                        true
                                ) {
                                    private String message;

                                    @Override
                                    public void run(@NotNull ProgressIndicator progressIndicator) {
                                        try {
                                            message = client.getMessageCreateConnector(token);
                                        } catch (Exception e) {
                                            printAlert(statusPanel, "Error", e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onFinished() {
                                        super.onFinished();
                                        var status = taskStatusPoller.getStatus();
                                        printAlert(statusPanel,
                                                status != null ? status.name() : OperationResultStatusType.UNKNOWN.name(),
                                                message
                                        );
                                    }
                                });
                            }

                            taskStatusPoller.stopPolling();
                            ((Timer) event.getSource()).stop();
                        }
                    });

                    taskStatusPoller.startPolling(() -> {
                        try {
                            return client.getStatusCreateConnector(token);
                        } catch (Exception e) {
                            taskStatusPoller.stopPolling();
                            timer.stop();
                            printAlert(statusPanel, "Error", e.getMessage());
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
    public JComponent getComponent() {
        return mainPanel;
    }

    private void printAlert(StatusPanel statusPanel, String title, String msg) {
        ApplicationManager.getApplication().invokeLater(() -> {
            mainPanel.removeAll();
            mainPanel.add(statusPanel.showAlertPanel(
                    title,
                    msg,
                    new JBColor(Gray._255, Gray._255)
            ));
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }
}
