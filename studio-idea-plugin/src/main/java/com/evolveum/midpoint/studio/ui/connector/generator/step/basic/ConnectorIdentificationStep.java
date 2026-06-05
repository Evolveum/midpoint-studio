package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.other.StatusPanel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectorIdentificationStep extends ConnectorGeneratorWizardStep {

    private final MidPointClient client;
    private final ConnectorGeneratorDataModel dataModel;
    private ConnectorIdentification connectoridentification;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    private boolean initialized = false;

    public ConnectorIdentificationStep(
            ConnectorGeneratorBasicWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            StepStateBadge.State state
    ) {
        super(wizardContext, state);
        this.client = client;
        this.dataModel = dataModel;
        mainPanel.setName("Connector identification");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;

            setState(StepStateBadge.State.IN_PROGRESS);

            connectoridentification = new ConnectorIdentification(dataModel);
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

        AtomicReference<Exception> error = new AtomicReference<>();

        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            try {
                var connectorDevelopmentType = dataModel.connectorDevelopmentType;
                var connDevConnectorType = getConnDevConnectorType();
                connectorDevelopmentType.setConnector(connDevConnectorType);
                connectorDevelopmentType.setName(PolyStringType.fromOrig(connDevConnectorType.getGroupId()
                        + ":" + connDevConnectorType.getArtifactId()
                        + ":" + connDevConnectorType.getVersion()));
                dataModel.connectorDevelopmentType = client.upsert(connectorDevelopmentType.asPrismObject(), null);
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
