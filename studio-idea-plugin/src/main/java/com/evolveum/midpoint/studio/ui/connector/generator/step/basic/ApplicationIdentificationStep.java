package com.evolveum.midpoint.studio.ui.connector.generator.step.basic;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevApplicationInfoType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDeploymentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevIntegrationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.intellij.ide.wizard.CommitStepException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ApplicationIdentificationStep extends ConnectorGeneratorGeneralWizardStep {

    private ApplicationIdentification applicationIdentification;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    private final String COMBO_BOX_ITEM_UNDEFINED = "Undefined";

    public ApplicationIdentificationStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        super( wizardContext, client, dataModel,state, isHeader);
        mainPanel.setName("Application Identification");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;
            applicationIdentification = new ApplicationIdentification(getDataModel());
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

        try {
            var connectorDevelopmentType = new ConnectorDevelopmentType();
            connectorDevelopmentType.setApplication(getConnDevApplicationInfoType());
            connectorDevelopmentType.setName(
                    connectorDevelopmentType.getApplication().getApplicationName()
            );

            upsertConnectorDevelopmentType(connectorDevelopmentType);
        } catch (Exception ex) {
            throw new CommitStepException("Couldn't upsert connector development type");
        }

        setState(GenerateConnectorBadge.State.COMPLETE);

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
    public JComponent getComponent() {
        return mainPanel;
    }
}
