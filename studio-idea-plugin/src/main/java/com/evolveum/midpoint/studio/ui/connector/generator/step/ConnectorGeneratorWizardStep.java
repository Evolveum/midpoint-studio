package com.evolveum.midpoint.studio.ui.connector.generator.step;

import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorBasicWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.ConnectorGeneratorWizard;
import com.evolveum.midpoint.studio.ui.connector.generator.StepStateBadge;
import com.intellij.ide.wizard.StepAdapter;

public class ConnectorGeneratorWizardStep extends StepAdapter {

    private final ConnectorGeneratorBasicWizard wizardBasicContext;
    private final ConnectorGeneratorWizard wizardContext;
    private StepStateBadge.State state;
    private boolean header = false;
    private boolean canGoNext = false;

    public ConnectorGeneratorWizardStep(ConnectorGeneratorBasicWizard wizardBasicContext, StepStateBadge.State state) {
        this.wizardBasicContext = wizardBasicContext;
        this.wizardContext = null;
        this.state = state;
    }

    public ConnectorGeneratorWizardStep(ConnectorGeneratorWizard wizardContext, StepStateBadge.State state, boolean header) {
        this.wizardContext = wizardContext;
        this.wizardBasicContext = null;
        this.state = state;
        this.header = header;
    }

    public ConnectorGeneratorBasicWizard getWizardBasicContext() {
        return wizardBasicContext;
    }

    public ConnectorGeneratorWizard getWizardContext() {
        return wizardContext;
    }

    public StepStateBadge.State getState() {
        return state;
    }

    public void setState(StepStateBadge.State state) {
        this.state = state;
    }

    protected void canGoNext(boolean canGoNext) {
        this.canGoNext = canGoNext;
    }

    public boolean isCanGoNext() {
        return canGoNext;
    }

    public boolean isHeader() {
        return header;
    }
}
