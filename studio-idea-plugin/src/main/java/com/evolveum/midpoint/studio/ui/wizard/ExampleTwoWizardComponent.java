package com.evolveum.midpoint.studio.ui.wizard;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Dominik.
 */
public class ExampleTwoWizardComponent extends DialogWrapper {

    private JPanel panel;

    public ExampleTwoWizardComponent() {
        super(true);
        init();
        setTitle("Step 2: Start Wizard");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        panel = new JPanel();
        panel.add(new JLabel("Welcome to Step 2!"));
        return panel;
    }
}
