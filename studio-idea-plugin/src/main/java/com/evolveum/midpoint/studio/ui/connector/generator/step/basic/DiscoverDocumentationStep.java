package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.service.TaskStatusPoller;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDiscoverDocumentationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class DiscoverDocumentationStep extends ConnectorGeneratorGeneralWizardStep {

    private final TaskStatusPoller taskStatusPoller;
    private DiscoverDocumentation discoverDocumentation;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public DiscoverDocumentationStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        super(wizardContext, client, dataModel, state, isHeader);
        this.taskStatusPoller = ApplicationManager.getApplication().getService(TaskStatusPoller.class);
        mainPanel.setName("Documentation");
    }

    @Override
    public void _init() {
        if (!initialized) {
            initialized = true;
            setState(GenerateConnectorBadge.State.IN_PROGRESS);
            discoverDocumentation = new DiscoverDocumentation(getDataModel());

            ProgressManager.getInstance().run(new Task.Backgroundable(getClient().getProject(),
                    "Discover Documentation submit operation",
                    true
            ) {

                private String token;

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        token = getClient().submitOperationDiscoverDocumentation(getDataModel().connectorDevelopmentType.getOid());
                    } catch (SchemaException | IOException | AuthenticationException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onSuccess() {

                    var statusPanel = discoverDocumentation.getStatusPanel();

                    if (token == null) {
                        printAlert(mainPanel, statusPanel, "Error", "Token is Null");
                        setState(GenerateConnectorBadge.State.FIXING);
                        return;
                    }

                    mainPanel.add(statusPanel.showLoadingPanel(
                        "Identifying Documentation...",
                        "Analyzing your target application details to locate the right documentation.",
                        0
                    ));

                    var timer = new Timer(1000, event -> {
                        statusPanel.updateElapsed(taskStatusPoller.getElapsedTime().toSeconds());

                        if (taskStatusPoller.getStatus() != null
                                && !OperationResultStatusType.IN_PROGRESS.equals(taskStatusPoller.getStatus())
                        ) {
                            if (OperationResultStatusType.SUCCESS.equals(taskStatusPoller.getStatus())) {
                                ProgressManager.getInstance().run(new Backgroundable(getClient().getProject(),
                                        "DiscoverDocumentation get result",
                                        true
                                ) {
                                    private ConnDevDiscoverDocumentationResultType result;

                                    @Override
                                    public void run(@NotNull ProgressIndicator progressIndicator) {
                                        try {
                                            result = getClient().getResultDiscoverDocumentation(token);
                                        } catch (Exception e) {
                                            printAlert(mainPanel, statusPanel, "Error", e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onSuccess() {
                                        super.onSuccess();

                                        mainPanel.removeAll();

                                        if (result != null) {
                                            if (!result.getDocumentation().isEmpty()) {
                                                discoverDocumentation.fillDocumentationList(result.getDocumentation());
                                            } else {
                                                discoverDocumentation.getItemDocPanel().add(statusPanel.showAlertPanel(
                                                        OperationResultStatusType.WARNING.name(),
                                                        "No documentation found",
                                                        new JBColor(Gray._255, Gray._255)
                                                ));
                                            }
                                            mainPanel.add(discoverDocumentation.getMainPanel());
                                            canGoNext(true);
                                            getWizardContext().updateWizardButtons();
                                        } else {
                                            printAlert(mainPanel, statusPanel, OperationResultStatusType.UNKNOWN.name(), "Result Null");
                                        }

                                        mainPanel.revalidate();
                                        mainPanel.repaint();
                                    }
                                });
                            } else {
                                ProgressManager.getInstance().run(new Backgroundable(getClient().getProject(),
                                        "DiscoverDocumentation get message",
                                        true
                                ) {
                                    private String message;

                                    @Override
                                    public void run(@NotNull ProgressIndicator progressIndicator) {
                                        try {
                                            message = getClient().getMessageDiscoverDocumentation(token);
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
                            return getClient().getStatusDiscoverDocumentation(token);
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

        try {
            var selectedDocumentationSources = discoverDocumentation.getSelectedDocumentationSources();
            if (selectedDocumentationSources != null && !selectedDocumentationSources.isEmpty()) {
                selectedDocumentationSources.forEach(source ->
                        getDataModel().connectorDevelopmentType.documentationSource(source.clone())
                );
            }
        } catch (Exception ex) {
            throw new CommitStepException(ex.getMessage());
        }

        try {
            upsertConnectorDevelopmentType(getDataModel().connectorDevelopmentType);
        }  catch (Exception ex) {
            throw new CommitStepException("Couldn't upsert connector development type");
        }

        setState(GenerateConnectorBadge.State.COMPLETE);

        super._commit(finishChosen);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
