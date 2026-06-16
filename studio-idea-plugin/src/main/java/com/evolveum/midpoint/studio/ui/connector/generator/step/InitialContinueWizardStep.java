package com.evolveum.midpoint.studio.ui.connector.generator.step;

import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;

import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.progress.ProgressManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class InitialContinueWizardStep extends ConnectorGeneratorGeneralWizardStep {

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private InitialContinueWizard initialContinueWizard;
    private final String oid;

    public InitialContinueWizardStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader,
            String oid
    ) {
        super(wizardContext, client, dataModel, state, isHeader);
        this.oid = oid;
        mainPanel.setName("Initial Continue");
    }

    @Override
    public void _init() {
        if (!initialized) {
            initialized = true;

            initialContinueWizard = new InitialContinueWizard();
            mainPanel.add(initialContinueWizard.getMainPanel());

            boolean success = ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                try {
                    getDataModel().connectorDevelopmentType = getClient().continueFrom(oid);
                } catch (SchemaException | AuthenticationException | IOException ex) {
                    printAlert(mainPanel, initialContinueWizard.getStatusPanel(), "Error", ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }, "ContinueFrom Development Connector", true, getClient().getProject());

            if (!success) {
                printAlert(mainPanel,
                        initialContinueWizard.getStatusPanel(),
                        "Error",
                        "Continue from Development Connector failed"
                );
            } else {
                canGoNext(true);
            }
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
        }

        super._commit(finishChosen);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
