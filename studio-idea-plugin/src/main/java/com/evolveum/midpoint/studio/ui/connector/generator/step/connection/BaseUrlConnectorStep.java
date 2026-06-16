package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.intellij.ide.wizard.CommitStepException;

import javax.swing.*;
import java.awt.*;

public class BaseUrlConnectorStep extends ConnectorGeneratorGeneralWizardStep {

    private BaseUrlConnector baseUrlConnector;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public BaseUrlConnectorStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        super(wizardContext, client, dataModel, state, isHeader);
        mainPanel.setName("Base URL specification");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;
            baseUrlConnector = new BaseUrlConnector(getDataModel());
            mainPanel.add(baseUrlConnector.getMainPanel());

            var restBaseAddressValue = baseUrlConnector.getRestBaseAddressField().getText();
            if (restBaseAddressValue != null && !restBaseAddressValue.isEmpty()) {
                getDataModel().connectorDevelopmentType.getApplication().setBaseApiEndpoint(restBaseAddressValue);
                canGoNext(true);
            }

            setState(GenerateConnectorBadge.State.IN_PROGRESS);
        }

        super._init();
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {

        if (baseUrlConnector.getRestBaseAddressField().getText() == null ||
                baseUrlConnector.getRestBaseAddressField().getText().isBlank()) {
            throw new CommitStepException("Field Base Api Endpoint is required");
        }

        if (getDataModel().connectorDevelopmentType.getApplication() == null) {
            throw new CommitStepException("Application is Null");
        }

        try {
            getDataModel().connectorDevelopmentType.getApplication().setBaseApiEndpoint(
                    baseUrlConnector.getRestBaseAddressField().getText()
            );
            upsertConnectorDevelopmentType(getDataModel().connectorDevelopmentType);
            canGoNext(true);
        } catch (Exception ex) {
            throw new CommitStepException("Couldn't upsert connector development type");
        }

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
