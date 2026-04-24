package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.ui.components.JBPanel;

import java.awt.*;

public class InitialPanel extends JBPanel<InitialPanel> implements WizardContent {

    public InitialPanel() {
        setLayout(new BorderLayout());
        JBPanel<?> contentPanel = new JBPanel<>(new BorderLayout());
        contentPanel.add(new Label("Generate connector with help of AI"), BorderLayout.CENTER);
        add(contentPanel);
    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
