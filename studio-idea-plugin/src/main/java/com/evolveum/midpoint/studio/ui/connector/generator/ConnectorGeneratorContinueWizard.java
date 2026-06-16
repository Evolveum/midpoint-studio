package com.evolveum.midpoint.studio.ui.connector.generator;

import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.InitialContinueWizardStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.CreateConnectorStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.connection.*;
import com.evolveum.midpoint.studio.ui.connector.generator.step.objectclass.ObjectClassStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.next.NextStepsConnectorStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.relation.RelationStep;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

public class ConnectorGeneratorContinueWizard extends ConnectorGeneratorWizard {

    private final MidPointClient client;
    private final ConnectorGeneratorDataModel dataModel = new ConnectorGeneratorDataModel();
    private final String oid;

    public ConnectorGeneratorContinueWizard(@NotNull MidPointClient client, @NotNull String oid) {
        super(client.getProject());
        this.client = client;
        this.oid = oid;
        getHelpButton().setVisible(false);
        setSize(1300, 600);
        buildSteps();
        init();
    }

    @Override
    protected void buildSteps() {
        myWizardStepsList.clear();

        myWizardStepsList.add(new InitialContinueWizardStep(this, client, dataModel, GenerateConnectorBadge.State.IN_PROGRESS, false, oid));

        myWizardStepsList.add(new ConnectionStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, true));
        myWizardStepsList.add(new BaseUrlConnectorStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
        myWizardStepsList.add(new AuthMethodSupportStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
        myWizardStepsList.add(new AuthScriptsConnectorStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
        myWizardStepsList.add(new TestConnectionStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));
        myWizardStepsList.add(new ConnectionConnectorStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, false));

        myWizardStepsList.add(new ObjectClassStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, true));

        myWizardStepsList.add(new RelationStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, true));

        myWizardStepsList.add(new NextStepsConnectorStep(this, client, dataModel, GenerateConnectorBadge.State.NONE, true));

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
