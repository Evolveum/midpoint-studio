package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.next;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.ApplicationIdentificationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.ui.components.JBPanel;

import java.awt.*;

public class InitialNextPanel extends JBPanel<InitialNextPanel> implements WizardContent {

    public InitialNextPanel() {
        setLayout(new BorderLayout());
        JBPanel<?> contentPanel = new JBPanel<>(new BorderLayout());
        contentPanel.add(new Label("Next Group"), BorderLayout.CENTER);
        add(contentPanel);
    }

    @Override
    public void beforeChangeAction() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
