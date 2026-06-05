package com.evolveum.midpoint.studio.ui.connector.generator.step.objectclass;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.intellij.ide.wizard.StepAdapter;

public class SearchAllEndpointsConnectorStep extends ConnectorGeneratorWizardStep {
    public SearchAllEndpointsConnectorStep(ConnectorGeneratorBasicWizard wizardContext, StepStateBadge.State step) {
        super(wizardContext, step);
    }
}
