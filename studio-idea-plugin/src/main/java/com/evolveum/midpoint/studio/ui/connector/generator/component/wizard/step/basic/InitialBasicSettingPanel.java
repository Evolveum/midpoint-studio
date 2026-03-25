package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic;

import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.ui.components.JBPanel;

import java.awt.*;

public class InitialBasicSettingPanel extends JBPanel<BasicSettingPanel> implements WizardContent {

    public InitialBasicSettingPanel() {
        setLayout(new BorderLayout());
        JBPanel<?> contentPanel = new JBPanel<>(new BorderLayout());
        contentPanel.add(new Label("Basic settings Group"), BorderLayout.CENTER);
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
