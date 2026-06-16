package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.intellij.ide.wizard.CommitStepException;

import javax.swing.*;
import java.awt.*;

public class EndpointConnectorStep extends ConnectorGeneratorGeneralWizardStep {

    private AuthScriptsConnector authScriptsConnector;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public EndpointConnectorStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        super(wizardContext, client, dataModel, state, isHeader);
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;

//            baseUrlConnector = new BaseUrlConnector(dataModel);
//            mainPanel.add(baseUrlConnector.getMainPanel());
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
