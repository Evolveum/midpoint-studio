package com.evolveum.midpoint.studio.ui.wizard;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by Dominik.
 */
public class WizardDialog extends DialogWrapper {
    private JPanel stepPanel;
    private CardLayout cardLayout;

    private int currentStep = 0;
    private final int totalSteps = 3;

    public WizardDialog() {
        super(true);
        init();
        setTitle("Example Component Wizard");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        cardLayout = new CardLayout();
        stepPanel = new JPanel(cardLayout);

        // Step panels
        stepPanel.add(createStep("Step 1: Welcome"), "0");
        stepPanel.add(createStep("Step 2: Details"), "1");
        stepPanel.add(createStep("Step 3: Finish"), "2");

        mainPanel.add(stepPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createStep(String labelText) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(labelText));
        return panel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getCancelAction(), new BackAction(), new NextAction()};
    }

    private void updateStep() {
        cardLayout.show(stepPanel, String.valueOf(currentStep));
        setOKActionEnabled(currentStep == totalSteps - 1); // Only enable OK on last step
    }

    private class NextAction extends DialogWrapperAction {
        protected NextAction() {
            super("Next");
        }

        @Override
        protected void doAction(ActionEvent e) {
            if (currentStep < totalSteps - 1) {
                currentStep++;
                updateStep();
            } else {
                close(OK_EXIT_CODE); // Finish
            }
        }
    }

    private class BackAction extends DialogWrapperAction {
        protected BackAction() {
            super("Back");
        }

        @Override
        protected void doAction(ActionEvent e) {
            if (currentStep > 0) {
                currentStep--;
                updateStep();
            }
        }
    }

    @Override
    public void show() {
        super.show();
        updateStep();
    }
}
