package com.evolveum.midpoint.studio.ui.connector.generator;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.InitialContinueConnectorDevelopmentStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.connection.*;
import com.evolveum.midpoint.studio.ui.connector.generator.step.objectclass.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorDevelopmentType;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

public class ConnectorGeneratorContinueWizard extends ConnectorGeneratorWizard {

    private final MidPointClient client;
    private final ConnectorGeneratorDataModel dataModel = new ConnectorGeneratorDataModel();

    public ConnectorGeneratorContinueWizard(
            @NotNull MidPointClient client,
            @NotNull ConnectorDevelopmentType connectorDevelopmentType
    ) {
        super(client.getProject());
        this.client = client;
        dataModel.connectorDevelopmentType = connectorDevelopmentType;
        getHelpButton().setVisible(false);
        setSize(1300, 900);
        buildSteps();
        init();
    }

    @Override
    protected void buildSteps() {
        myWizardStepsList.clear();

//        myWizardStepsList.add(new InitialContinueConnectorDevelopmentStep(this, client, dataModel, GenerateConnectorBadge.State.IN_PROGRESS, true));
        myWizardStepsList.add(new BaseUrlSpecificationStep(this, client, dataModel, GenerateConnectorBadge.State.IN_PROGRESS, false));
        myWizardStepsList.add(new AuthMethodSupportStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
        myWizardStepsList.add(new AuthScriptsConnectorStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
        myWizardStepsList.add(new CredentialsConnectorStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
        myWizardStepsList.add(new TestConnectionStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));

//        myWizardStepsList.add(new ObjectClassesStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, true));
//        myWizardStepsList.add(new SchemaScriptValidationStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
//        myWizardStepsList.add(new SchemaValidationStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
//        myWizardStepsList.add(new SearchEndpointsStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
//        myWizardStepsList.add(new SearchAllScriptValidationStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
//        myWizardStepsList.add(new SearchResultStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));

        myWizardStepsList.forEach(this::addStep);
        stepNavigationItems = new JBList<>(visibleListModel);

        updateNavigationMenuByLiveStates();
    }

    @Override
    protected void doOKAction() {
        // TODO what at the end?
        super.doOKAction();
    }
}
