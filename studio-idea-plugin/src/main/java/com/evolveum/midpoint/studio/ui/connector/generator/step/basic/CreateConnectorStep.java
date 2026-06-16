package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.service.TaskStatusPoller;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevCreateConnectorResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class CreateConnectorStep extends ConnectorGeneratorGeneralWizardStep {

    private final TaskStatusPoller taskStatusPoller;
    private CreateConnector createConnector;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public CreateConnectorStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        super(wizardContext, client, dataModel, state, isHeader);
        this.taskStatusPoller = ApplicationManager.getApplication().getService(TaskStatusPoller.class);
        mainPanel.setName("Creating Connector");
    }

    @Override
    public void _init() {
        if (!initialized) {
            initialized = true;

            setState(GenerateConnectorBadge.State.IN_PROGRESS);

            createConnector = new CreateConnector();

            ProgressManager.getInstance().run(new Task.Backgroundable(getClient().getProject(),
                    "CreateConnector submit operation",
                    true
            ) {

                private String token;

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        token = getClient().submitOperationCreateConnector(getDataModel().connectorDevelopmentType.getOid());
                    } catch (SchemaException | IOException | AuthenticationException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onSuccess() {
                    var statusPanel = createConnector.getStatusPanel();

                    if (token == null) {

                        printAlert(mainPanel, statusPanel, "Error", "Token is Null");
                        return;
                    }

                    mainPanel.add(statusPanel.showLoadingPanel(
                            "Creating Connector...",
                            "We use the connector's basic information to create a test instance for development and testing purposes.",
                            0
                    ));
                    var timer = new Timer(1000, event -> {
                        statusPanel.updateElapsed(taskStatusPoller.getElapsedTime().toSeconds());

                        if (taskStatusPoller.getStatus() != null
                                && !OperationResultStatusType.IN_PROGRESS.equals(taskStatusPoller.getStatus())
                        ) {
                            if (OperationResultStatusType.SUCCESS.equals(taskStatusPoller.getStatus())) {
                                ProgressManager.getInstance().run(new Backgroundable(getClient().getProject(),
                                        "CreateConnector get result",
                                        true
                                ) {
                                    private ConnDevCreateConnectorResultType result;

                                    @Override
                                    public void run(@NotNull ProgressIndicator progressIndicator) {
                                        try {
                                            result = getClient().getResultCreateConnector(token);
                                        } catch (Exception e) {
                                            printAlert(mainPanel, statusPanel, "Error", e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onSuccess() {
                                        super.onSuccess();

                                        if (result != null) {
                                            if (result.getConnectorRef() != null) {
                                                try {
                                                    var prismContext = getClient().getPrismContext();
                                                    PrismObject<ResourceType> resourceObject = prismContext.createObject(ResourceType.class);
                                                    ResourceType resourceType = resourceObject.asObjectable();
                                                    resourceType.setName(getDataModel().connectorDevelopmentType.getName());
                                                    ObjectReferenceType clonedRef = result.getConnectorRef().clone();
                                                    resourceType.setConnectorRef(clonedRef);
                                                    getClient().upload(resourceType.asPrismObject(), null);
                                                } catch (SchemaException | AuthenticationException | IOException e) {
                                                    throw new RuntimeException(e);
                                                }

                                                printAlert(mainPanel,
                                                        statusPanel,
                                                        OperationResultStatusType.SUCCESS.name(),
                                                        "Successfully Generated Connector Development"
                                                );

                                                canGoNext(true);
                                                getWizardContext().updateWizardButtons();
                                                setState(GenerateConnectorBadge.State.COMPLETE);
                                            } else {
                                                printAlert(mainPanel,
                                                        statusPanel,
                                                        OperationResultStatusType.UNKNOWN.name(),
                                                        "ConnectorRef Null"
                                                );
                                            }
                                        } else {
                                            printAlert(mainPanel,
                                                    statusPanel,
                                                    OperationResultStatusType.UNKNOWN.name(),
                                                    "Result Null"
                                            );
                                        }
                                    }
                                });
                            } else {
                                ProgressManager.getInstance().run(new Backgroundable(getClient().getProject(),
                                        "CreateConnector get message",
                                        true
                                ) {
                                    private String message;

                                    @Override
                                    public void run(@NotNull ProgressIndicator progressIndicator) {
                                        try {
                                            message = getClient().getMessageCreateConnector(token);
                                        } catch (Exception e) {
                                            printAlert(mainPanel, statusPanel, "Error", e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onFinished() {
                                        super.onFinished();
                                        var status = taskStatusPoller.getStatus();
                                        printAlert(mainPanel,
                                                statusPanel,
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
                            return getClient().getStatusCreateConnector(token);
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
    public JComponent getComponent() {
        return mainPanel;
    }
}
