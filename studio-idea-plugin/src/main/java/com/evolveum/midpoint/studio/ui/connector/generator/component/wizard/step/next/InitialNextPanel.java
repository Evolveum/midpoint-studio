package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.next;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.BasicSettingPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.ui.components.JBPanel;

import java.awt.*;

public class InitialNextPanel extends JBPanel<BasicSettingPanel> implements WizardContent {

    public InitialNextPanel() {
        setLayout(new BorderLayout());
        JBPanel<?> contentPanel = new JBPanel<>(new BorderLayout());
        contentPanel.add(new Label("Next Group"), BorderLayout.CENTER);
        add(contentPanel);
    }

    @Override
    public void onStateChanged() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
