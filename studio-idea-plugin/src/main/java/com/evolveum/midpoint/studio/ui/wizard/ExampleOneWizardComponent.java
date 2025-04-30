package com.evolveum.midpoint.studio.ui.wizard;

import com.intellij.openapi.ui.DialogWrapper;

import org.jetbrains.annotations.Nullable;
import javax.swing.*;

/**
 * Created by Dominik.
 */
public class ExampleOneWizardComponent extends DialogWrapper {

    private JPanel panel;

    public ExampleOneWizardComponent() {
        super(true);
        init();
        setTitle("Step 1: Start Wizard");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        panel = new JPanel();
        panel.add(new JLabel("Welcome to Step 1!"));
        return panel;
    }

}
