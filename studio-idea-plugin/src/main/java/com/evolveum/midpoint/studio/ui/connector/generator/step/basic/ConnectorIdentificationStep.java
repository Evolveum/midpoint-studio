package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.ide.wizard.CommitStepException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ConnectorIdentificationStep extends ConnectorGeneratorGeneralWizardStep {

    private ConnectorIdentification connectoridentification;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public ConnectorIdentificationStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        super(wizardContext, client, dataModel, state, isHeader);
        mainPanel.setName("Connector identification");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;

            setState(GenerateConnectorBadge.State.IN_PROGRESS);

            connectoridentification = new ConnectorIdentification(getDataModel());
            mainPanel.add(connectoridentification.getMainPanel());
            canGoNext(true);
        }

        super._init();
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {

        if (connectoridentification.getGroupIdTextField().getText() == null ||
                connectoridentification.getGroupIdTextField().getText().isBlank()) {
            throw new CommitStepException("Group ID is required field");
        }

        if (connectoridentification.getArtifactIdTextField().getText() == null ||
                connectoridentification.getArtifactIdTextField().getText().isBlank()) {
            throw new CommitStepException("Artifact Id is required field");
        }

        if (connectoridentification.getVersionTextField().getText() == null ||
                connectoridentification.getVersionTextField().getText().isBlank()) {
            throw new CommitStepException("Version is required field");
        }

        try {
            var connectorDevelopmentType = getDataModel().connectorDevelopmentType;
            var connDevConnectorType = getConnDevConnectorType();
            connectorDevelopmentType.setConnector(connDevConnectorType);
            connectorDevelopmentType.setName(PolyStringType.fromOrig(connDevConnectorType.getGroupId()
                    + ":" + connDevConnectorType.getArtifactId()
                    + ":" + connDevConnectorType.getVersion()));

            upsertConnectorDevelopmentType(connectorDevelopmentType);
        } catch (Exception ex) {
            throw new CommitStepException("Couldn't upsert connector development type");
        }

        setState(GenerateConnectorBadge.State.COMPLETE);

        super._commit(finishChosen);
    }

    private @NotNull ConnDevConnectorType getConnDevConnectorType() {
        var connDevConnectorType = new ConnDevConnectorType();
        connDevConnectorType.setGroupId(connectoridentification.getGroupIdTextField().getText());
        connDevConnectorType.setArtifactId(connectoridentification.getArtifactIdTextField().getText());
        connDevConnectorType.setDisplayName(new PolyStringType(connectoridentification.getDisplayNameTextField().getText()));
        connDevConnectorType.setDescription(connectoridentification.getDescriptionTextArea().getText());
        connDevConnectorType.setVersion(connectoridentification.getVersionTextField().getText());
        return connDevConnectorType;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
