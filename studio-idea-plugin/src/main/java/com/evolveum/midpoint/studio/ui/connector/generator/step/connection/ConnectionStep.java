package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SmartIntegrationOperationStatusInfoType;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ConnectionStep extends ConnectorGeneratorGeneralWizardStep {

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
                        printAlertPanel(mainPanel, statusPanel, "Error", "Token is Null");
                        setState(GenerateConnectorBadge.State.FIXING);
                        return;
                    }

//                    waitingPanel(
//                            mainPanel,
//                            statusPanel,
//                            "Identifying Connection Possibilities...",
//                            """
//                                This involves exploring and determining the available options for establishing
//                                a connection, including supported protocols, authentication methods, and endpoints.
//                                """,
//                            token
//                    );

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

    @Override
    public SmartIntegrationOperationStatusInfoType getStatusInfo(
            String token
    ) throws SchemaException, AuthenticationException, IOException {
        // FIXME
        return null;
//        return getClient().getStatusInfo(token);
    }
}
