package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.ApplicationIdentification;
import com.intellij.ide.wizard.CommitStepException;

import javax.swing.*;
import java.awt.*;

public class AuthScriptsConnectorStep extends ConnectorGeneratorWizardStep {

    private final MidPointClient client;
    private final ConnectorGeneratorDataModel dataModel;
    private AuthScriptsConnector authScriptsConnector;
    private JPanel mainPanel = new JPanel(new BorderLayout());

    private boolean initialized = false;

    public AuthScriptsConnectorStep(
            ConnectorGeneratorBasicWizard wizardContext,
            StepStateBadge.State step,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel
    ) {
        super(wizardContext, step);
        this.client = client;
        this.dataModel = dataModel;
        mainPanel.setName("Application Identification");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;

//            authScriptsConnector = new AuthScriptsConnector(dataModel);
//            mainPanel.add(authScriptsConnector.getMainPanel());
            canGoNext(true);
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
