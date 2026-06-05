package com.evolveum.midpoint.studio.ui.connector.generator.step.relation;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.intellij.ide.wizard.StepAdapter;

public class RelationshipSelectConnectorStep extends ConnectorGeneratorWizardStep {

    public RelationshipSelectConnectorStep(ConnectorGeneratorBasicWizard wizardContext, StepStateBadge.State step) {
        super(wizardContext, step);
    }
}
