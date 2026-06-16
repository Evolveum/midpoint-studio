package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.smart.api.conndev.SupportedAuthorization;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorDataModel;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevAuthInfoType;
import com.intellij.ide.wizard.CommitStepException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class AuthMethodSupportStep extends ConnectorGeneratorGeneralWizardStep {

    private AuthMethodSupport authMethodSupport;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public AuthMethodSupportStep(
            ConnectorGeneratorWizard wizardContext,
            MidPointClient client,
            ConnectorGeneratorDataModel dataModel,
            GenerateConnectorBadge.State state,
            boolean isHeader
    ) {
        super(wizardContext, client, dataModel, state, isHeader);
        mainPanel.setName("Auth Method Support");
    }

    @Override
    public void _init() {

        if (!initialized) {
            initialized = true;

            authMethodSupport = new AuthMethodSupport(getDataModel());
            mainPanel.add(authMethodSupport.getMainPanel());

            refreshAuthOptions();
            authMethodSupport.getRecommendedOptionsBtn().addActionListener(e -> refreshAuthOptions());
            setState(GenerateConnectorBadge.State.IN_PROGRESS);
            canGoNext(true);
        }

        super._init();
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {

        var application = getDataModel().connectorDevelopmentType.getApplication();

        authMethodSupport.getSelectedItem().stream()
                .filter(new HashSet<>(application.getAuth())::add)
                .forEach(application::auth);

        try {
            upsertConnectorDevelopmentType(getDataModel().connectorDevelopmentType);
        }  catch (Exception ex) {
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

    private void refreshAuthOptions() {
        var recommendedOptionsBtn = authMethodSupport.getRecommendedOptionsBtn();
        var selected = recommendedOptionsBtn.isSelected();

        recommendedOptionsBtn.setText(
                selected
                        ? "Hide not recommended options"
                        : "Show all options");

        authMethodSupport.fillItemList(getValues(selected));
    }

    private ArrayList<ConnDevAuthInfoType> getValues(boolean showAllOptions) {
        var values = new ArrayList<>(
                getDataModel().connectorDevelopmentType.getConnector().getAuth());

        if (!showAllOptions) {
            values.removeIf(v -> !Boolean.TRUE.equals(v.isRecommended()));
            return values;
        }

        var existingTypes = values.stream()
                .map(ConnDevAuthInfoType::getType)
                .collect(Collectors.toSet());

        Arrays.stream(SupportedAuthorization.values())
                .filter(auth -> auth != SupportedAuthorization.NONE)
                .map(SupportedAuthorization::crateBasicInformation)
                .filter(info -> existingTypes.add(info.getType()))
                .forEach(values::add);

        return values;
    }
}
