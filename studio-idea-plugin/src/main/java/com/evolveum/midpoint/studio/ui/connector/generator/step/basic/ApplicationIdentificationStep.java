package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class ApplicationIdentificationStep extends ConnectorGeneratorWizardStep {

    private final MidPointClient client;
    private final ConnectorGeneratorDataModel dataModel;
    private ApplicationIdentification applicationIdentification;
    private JPanel mainPanel = new JPanel(new BorderLayout());

    private final String COMBO_BOX_ITEM_UNDEFINED = "Undefined";

    private boolean initialized = false;

    public ApplicationIdentificationStep(
            ConnectorGeneratorBasicWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            StepStateBadge.State state
    ) {
        super(wizardContext, state);
        this.client = client;
        this.dataModel = dataModel;
        mainPanel.setName("Application Identification");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;

            applicationIdentification = new ApplicationIdentification(dataModel);
            mainPanel.add(applicationIdentification.getMainPanel());
            canGoNext(true);
        }

        super._init();
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {

        if (applicationIdentification.getApplicationNameField().getText() == null ||
                applicationIdentification.getApplicationNameField().getText().isBlank()) {
            throw new CommitStepException("Field Application Name is required");
        }

        if (applicationIdentification.getIntegrationTypeCombo().getSelectedItem() == null ||
                applicationIdentification.getIntegrationTypeCombo().getSelectedItem().equals(COMBO_BOX_ITEM_UNDEFINED)) {
            throw new CommitStepException("Field Integration Type is required");
        }

        AtomicReference<Exception> error = new AtomicReference<>();

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
                error.set(e);
            }
        }, "Create ConnectorDevelopmentType Object", true, client.getProject());

        if (error.get() != null) {
            throw new CommitStepException(error.get().getMessage());
        } else if (dataModel.connectorDevelopmentType == null) {
            throw new CommitStepException(
                    "Failed to create ConnectorDevelopmentType object"
            );
        } else {
            setState(StepStateBadge.State.COMPLETE);
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
        return mainPanel;
    }
}
