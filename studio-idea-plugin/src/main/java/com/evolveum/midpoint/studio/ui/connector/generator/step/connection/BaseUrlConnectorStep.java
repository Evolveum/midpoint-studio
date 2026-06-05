package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.intellij.ide.wizard.CommitStepException;

import javax.swing.*;
import java.awt.*;

public class BaseUrlConnectorStep extends ConnectorGeneratorWizardStep {

    private final MidPointClient client;
    private final ConnectorGeneratorDataModel dataModel;
    private BaseUrlConnector baseUrlConnector;
    private JPanel mainPanel = new JPanel(new BorderLayout());

    private boolean initialized = false;

    public BaseUrlConnectorStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            StepStateBadge.State state
    ) {
        super(wizardContext, state);
        this.client = client;
        this.dataModel = dataModel;
        mainPanel.setName("Base Url Connector");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;

//            baseUrlConnector = new BaseUrlConnector(dataModel);
//            mainPanel.add(baseUrlConnector.getMainPanel());
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
