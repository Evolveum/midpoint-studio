package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.intellij.ide.wizard.CommitStepException;

import javax.swing.*;
import java.awt.*;

public class AuthScriptsConnectorStep extends ConnectorGeneratorGeneralWizardStep {

    private AuthScriptsConnector authScriptsConnector;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public AuthScriptsConnectorStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        super(wizardContext, client, dataModel, state, isHeader);
        mainPanel.setName("Application Identification");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;

            authScriptsConnector = new AuthScriptsConnector(getDataModel(), getClient().getProject());
            mainPanel.add(authScriptsConnector.getMainPanel());

            setState(GenerateConnectorBadge.State.IN_PROGRESS);
        }

        super._init();
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {
        super._commit(finishChosen);
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
