package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step.basic;

import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;

import javax.swing.*;
import java.awt.*;


public class InitialStep extends StepAdapter {

    private final JBPanel<?> panel = new JBPanel<>();

    public InitialStep() {
        panel.add(new JBLabel("Generate connector with help of AI"), BorderLayout.CENTER);
        panel.setName("Initial Panel");
    }

    @Override
    public void _init() {
    }

    @Override
    public void _commit(boolean b) throws CommitStepException {

    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }
}
