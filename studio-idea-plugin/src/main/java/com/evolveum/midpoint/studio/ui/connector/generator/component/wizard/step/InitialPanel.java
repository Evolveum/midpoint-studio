package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step;

import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.ui.components.JBPanel;

public class InitialPanel extends JBPanel<InitialPanel> implements WizardContent {

    @Override
    public void onStateChanged() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
