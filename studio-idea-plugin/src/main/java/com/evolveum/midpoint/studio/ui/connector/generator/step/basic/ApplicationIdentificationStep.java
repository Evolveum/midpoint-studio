package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ApplicationIdentificationStep extends StepAdapter {

    private final MidPointClient client;
    private final ConnectorGeneratorDataModel dataModel;
    private StepStateBadge.State state;
    private final ApplicationIdentification applicationIdentification;

    private final String COMBO_BOX_ITEM_UNDEFINED = "Undefined";

    public ApplicationIdentificationStep(
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            StepStateBadge.State state
    ) {
        this.client = client;
        this.dataModel = dataModel;
        this.state = state;
        this.applicationIdentification = new ApplicationIdentification(dataModel);
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {
        var connection = client.testConnection();

        if (!connection.success()) {
            throw new CommitStepException(connection.exception().getMessage());
        }

        if (applicationIdentification.getApplicationNameField().getText() == null ||
                applicationIdentification.getApplicationNameField().getText().isBlank()) {
            throw new CommitStepException("Field Application Name is required");
        }

        if (applicationIdentification.getIntegrationTypeCombo().getSelectedItem() == null ||
                applicationIdentification.getIntegrationTypeCombo().getSelectedItem().equals(COMBO_BOX_ITEM_UNDEFINED)) {
            throw new CommitStepException("Field Integration Type is required");
        }

        try {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {

                var connectorDevelopmentType = new ConnectorDevelopmentType();
                connectorDevelopmentType.setApplication(getConnDevApplicationInfoType());
                connectorDevelopmentType.setName(
                        connectorDevelopmentType.getApplication().getApplicationName()
                );

                try {
                    dataModel.connectorDevelopmentType =
                            client.upsert(connectorDevelopmentType.asPrismObject(), null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, "Create ConnectorDevelopmentType object", true, client.getProject());

            if (dataModel.connectorDevelopmentType == null) {
                throw new CommitStepException(
                        "Failed to create ConnectorDevelopmentType object"
                );
            }

        } catch (Exception ex) {
            throw new CommitStepException(ex.getMessage());
        }

        super._commit(finishChosen);
    }

    private @NotNull ConnDevApplicationInfoType getConnDevApplicationInfoType() {
        var connDevApplicationInfoType = new ConnDevApplicationInfoType();
        connDevApplicationInfoType.setApplicationName(new PolyStringType(applicationIdentification.getApplicationNameField().getText()));
        connDevApplicationInfoType.setVersion(applicationIdentification.getVersionField().getText());
        connDevApplicationInfoType.setDescription(applicationIdentification.getDescriptionArea().getText());
        connDevApplicationInfoType.setIntegrationType(
                applicationIdentification.getIntegrationTypeCombo().getSelectedItem() instanceof String s && !s.equals(COMBO_BOX_ITEM_UNDEFINED)
                        ? ConnDevIntegrationType.fromValue(s.toLowerCase())
                        : null
        );
        connDevApplicationInfoType.setDeploymentType(
                applicationIdentification.getDeploymentTypeCombo().getSelectedItem() instanceof String s && !s.equals(COMBO_BOX_ITEM_UNDEFINED)
                        ? ConnDevDeploymentType.fromValue(s.toLowerCase())
                        : null
        );
        return connDevApplicationInfoType;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public JComponent getComponent() {
        return applicationIdentification.getMainPanel();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    public StepStateBadge.State getState() {
        return state;
    }

    public void setState(StepStateBadge.State state) {
        this.state = state;
    }
}
