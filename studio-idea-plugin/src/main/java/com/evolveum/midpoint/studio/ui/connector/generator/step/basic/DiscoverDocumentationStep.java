package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.service.TaskStatusPoller;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.other.StatusPanel;
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
import java.util.concurrent.atomic.AtomicReference;

public class DiscoverDocumentationStep extends ConnectorGeneratorWizardStep {

    private final MidPointClient client;
    private final TaskStatusPoller taskStatusPoller;
    private final ConnectorGeneratorDataModel dataModel;
    private DiscoverDocumentation discoverDocumentation;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    private boolean initialized = false;

    public DiscoverDocumentationStep(
            ConnectorGeneratorBasicWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            StepStateBadge.State state
    ) {
        super(wizardContext, state);
        this.client = client;
        this.taskStatusPoller = ApplicationManager.getApplication().getService(TaskStatusPoller.class);
        this.dataModel = dataModel;

        mainPanel.setName("Documentation");
    }

    @Override
    public void _init() {
        if (!initialized) {
            initialized = true;
            setState(StepStateBadge.State.IN_PROGRESS);
            discoverDocumentation = new DiscoverDocumentation(dataModel);

            ProgressManager.getInstance().run(new Task.Backgroundable(client.getProject(),
                    "DiscoverDocumentationMessage submit operation",
                    true
            ) {

                private String token;

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        token = client.submitOperationDiscoverDocumentation(dataModel.connectorDevelopmentType.getOid());
                    } catch (SchemaException | IOException | AuthenticationException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onSuccess() {

                    var statusPanel = discoverDocumentation.getStatusPanel();

                    if (token == null) {
                        printAlert(statusPanel, "Error", "Token is Null");
                        setState(StepStateBadge.State.FIXING);
                        return;
                    }

                    ApplicationManager.getApplication().invokeLater(() -> mainPanel.add(statusPanel.showLoadingPanel(
                            "Identifying Documentation...",
                            "Analyzing your target application details to locate the right documentation.",
                            0
                    )));

                    var timer = new Timer(1000, event -> {
                        statusPanel.updateElapsed(taskStatusPoller.getElapsedTime().toSeconds());

                        if (taskStatusPoller.getStatus() != null
                                && !OperationResultStatusType.IN_PROGRESS.equals(taskStatusPoller.getStatus())
                        ) {
                            if (OperationResultStatusType.SUCCESS.equals(taskStatusPoller.getStatus())) {
                                ProgressManager.getInstance().run(new Backgroundable(client.getProject(),
                                        "DiscoverDocumentation get result",
                                        true
                                ) {
                                    private ConnDevDiscoverDocumentationResultType result;

                                    @Override
                                    public void run(@NotNull ProgressIndicator progressIndicator) {
                                        try {
                                            result = client.getResultDiscoverDocumentation(token);
                                        } catch (Exception e) {
                                            printAlert(statusPanel, "Error", e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onSuccess() {
                                        super.onSuccess();

                                        ApplicationManager.getApplication().invokeLater(() -> {
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
                                                getWizardBasicContext().updateWizardButtons();
                                            } else {
                                                printAlert(statusPanel, OperationResultStatusType.UNKNOWN.name(), "Result Null");
                                            }

                                            mainPanel.revalidate();
                                            mainPanel.repaint();
                                        });
                                    }
                                });
                            } else {
                                ProgressManager.getInstance().run(new Backgroundable(client.getProject(),
                                        "DiscoverDocumentation get message",
                                        true
                                ) {
                                    private String message;

                                    @Override
                                    public void run(@NotNull ProgressIndicator progressIndicator) {
                                        try {
                                            message = client.getMessageDiscoverDocumentation(token);
                                        } catch (Exception ex) {
                                            printAlert(statusPanel, "Error", ex.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onFinished() {
                                        super.onFinished();
                                        printAlert(statusPanel, taskStatusPoller.getStatus().name(), message);
                                    }
                                });
                            }

                            taskStatusPoller.stopPolling();
                            ((Timer) event.getSource()).stop();
                        }
                    });

                    taskStatusPoller.startPolling(() -> {
                        try {
                            return client.getStatusDiscoverDocumentation(token);
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
    public void _commit(boolean finishChosen) throws CommitStepException {

        try {
            var selectedDocumentationSources = discoverDocumentation.getSelectedDocumentationSources();
            if (selectedDocumentationSources != null && !selectedDocumentationSources.isEmpty()) {
                selectedDocumentationSources.forEach(source ->
                        dataModel.connectorDevelopmentType.documentationSource(source.clone())
                );
            }
        } catch (Exception ex) {
            throw new CommitStepException(ex.getMessage());
        }

        AtomicReference<Exception> error = new AtomicReference<>();

        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {

            var connectorDevelopmentType = dataModel.connectorDevelopmentType;

            try {
                dataModel.connectorDevelopmentType =
                        client.upsert(connectorDevelopmentType.asPrismObject(), null);
            } catch (Exception e) {
                error.set(e);
            }
        }, "Update ConnectorDevelopmentType Object", true, client.getProject());

        if (error.get() != null) {
            throw new CommitStepException(error.get().getMessage());
        } else if (dataModel.connectorDevelopmentType == null) {
            throw new CommitStepException(
                    "Failed to update ConnectorDevelopmentType object"
            );
        } else {
            setState(StepStateBadge.State.COMPLETE);
        }

        super._commit(finishChosen);
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
