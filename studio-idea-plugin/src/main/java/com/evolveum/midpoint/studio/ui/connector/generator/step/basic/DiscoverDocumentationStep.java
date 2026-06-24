package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SmartIntegrationOperationStatusInfoType;
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
                        token = getClient().submitOperationDiscoverDocumentation(
                                getDataModel().connectorDevelopmentType.getOid());
                    } catch (SchemaException | IOException | AuthenticationException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onSuccess() {

                    var statusPanel = discoverDocumentation.getStatusPanel();

                    if (token == null) {
                        printAlertPanel(mainPanel, statusPanel, "Error", "Token is Null");
                        setState(GenerateConnectorBadge.State.FIXING);
                        return;
                    }

                    printWaitingPanel(
                            mainPanel,
                            statusPanel,
                            "Identifying Documentation...",
                            """
                                  Analyzing your target application details to locate the right documentation.
                                  """
                    );

                    statusPanel.getElapsedLabel().start();

                    getResult(token).whenComplete((statusInfoResult, ex) -> {
                        if (ex != null) {
                            ApplicationManager.getApplication().invokeLater(() -> printAlertPanel(
                                    mainPanel,
                                    statusPanel,
                                    "Error",
                                    ex.getMessage()
                            ));
                        } else {
                            var result = statusInfoResult.getConnDevDiscoverDocumentationResult();

                            if (result != null && !result.getDocumentation().isEmpty()) {
                                discoverDocumentation.fillDocumentationList(result.getDocumentation());
                            } else {
                                discoverDocumentation.getItemDocPanel().add(statusPanel.showAlertPanel(
                                        OperationResultStatusType.WARNING.name(),
                                        "No documentation found",
                                        new JBColor(Gray._255, Gray._255)
                                ));
                            }

                            mainPanel.add(discoverDocumentation.getMainPanel());

                            // FIXME check if necessary updateWizardButtons
                            canGoNext(true);
                            getWizardContext().updateWizardButtons();
                        }

                        statusPanel.getElapsedLabel().stop();
                    });

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

            getDataModel().connectorDevelopmentType =
                    upsertConnectorDevelopmentType(getDataModel().connectorDevelopmentType);
        } catch (Exception ex) {
            throw new CommitStepException(ex.getMessage());
        }

        setState(GenerateConnectorBadge.State.COMPLETE);

        super._commit(finishChosen);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public SmartIntegrationOperationStatusInfoType getStatusInfo(
            String token
    ) throws SchemaException, AuthenticationException, IOException {
        return getClient().getStatusInfoDiscoverDocumentation(token);
    }
}
