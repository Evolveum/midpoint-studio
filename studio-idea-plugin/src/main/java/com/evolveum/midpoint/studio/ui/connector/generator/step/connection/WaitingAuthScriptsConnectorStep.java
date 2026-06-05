package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.intellij.ide.wizard.StepAdapter;

public class WaitingAuthScriptsConnectorStep extends ConnectorGeneratorWizardStep {
    public WaitingAuthScriptsConnectorStep(ConnectorGeneratorBasicWizard wizardContext, StepStateBadge.State step) {
        super(wizardContext, step);
    }
}
