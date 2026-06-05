package com.evolveum.midpoint.studio.ui.connector.generator.step.connection;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorWizardStep;
import com.intellij.ide.wizard.StepAdapter;

public class SupportedAuthMethodConnectorStep extends ConnectorGeneratorWizardStep {

    public SupportedAuthMethodConnectorStep(ConnectorGeneratorBasicWizard wizardContext, StepStateBadge.State step) {
        super(wizardContext, step);
    }
}
