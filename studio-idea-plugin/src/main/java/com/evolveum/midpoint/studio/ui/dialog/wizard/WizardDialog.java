/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.dialog.wizard;

import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class WizardDialog<T> extends DialogWrapper {

    protected final Project project;
    protected final List<JPanel> steps = new ArrayList<>();
    protected int currentStep = 0;
    protected final T dialogWizardContext;
    private final DialogWindowActionHandler actionHandler;

    private final JPanel contentPanel = new JPanel(new BorderLayout());
    protected final JButton nextButton = new JButton("Next");
    protected final JButton prevButton = new JButton("Back");

    public WizardDialog(Project project, String title, T dialogWizardContext, DialogWindowActionHandler actionHandler) {
        super(project);
        this.project = project;
        this.dialogWizardContext = dialogWizardContext;
        this.actionHandler = actionHandler;
        setTitle(title);
        init();

        if (actionHandler != null) {
            setOKButtonText(actionHandler.getOkButtonTitle());
            setCancelButtonText(actionHandler.getCancelButtonTitle());
        }
    }

    /**
     * Subclasses should override this to build wizard steps dynamically
     */
    protected abstract void buildSteps(T context);

    @Override
    protected @Nullable JComponent createCenterPanel() {
        buildSteps(dialogWizardContext);
        if (!steps.isEmpty()) {
            contentPanel.add(steps.get(0), BorderLayout.CENTER);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(contentPanel, BorderLayout.CENTER);

        if (steps.size() > 1) {
            // Navigation buttons
            JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            prevButton.setEnabled(false);
            prevButton.addActionListener(e -> showStep(currentStep - 1));
            nextButton.addActionListener(e -> showStep(currentStep + 1));

            navPanel.add(prevButton);
            navPanel.add(nextButton);
            wrapper.add(navPanel, BorderLayout.SOUTH);
        }

        wrapper.setPreferredSize(new Dimension(500, 300));
        return wrapper;
    }

    private void showStep(int step) {
        if (step < 0 || step >= steps.size()) return;

        contentPanel.removeAll();
        contentPanel.add(steps.get(step), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        currentStep = step;
        prevButton.setEnabled(step > 0);
        nextButton.setEnabled(step < steps.size() - 1);
    }

    @Override
    protected void doOKAction() {
        actionHandler.onOk();
        super.doOKAction();
    }

    @Override
    protected void applyFields() {
        actionHandler.onApply();
        super.applyFields();
    }

    @Override
    public void doCancelAction() {
        actionHandler.onCancel();
        super.doCancelAction();
    }

    @Override
    protected Action @NotNull [] createActions() {
        Action okAction = null;
        Action applyAction = null;
        Action cancelAction = null;

        if (actionHandler.isOkButtonVisible()) {
            okAction = getOKAction();
            okAction.putValue(Action.NAME, actionHandler.getOkButtonTitle());
            okAction.setEnabled(actionHandler.isOkButtonEnabled());
        }

        if (actionHandler.isApplyButtonVisible()) {
            applyAction = new DialogWrapperAction(actionHandler.getApplyButtonTitle()) {
                @Override
                protected void doAction(ActionEvent e) {
                    actionHandler.onApply();
                }
            };
            applyAction.setEnabled(actionHandler.isApplyButtonEnabled());
        }

        if (actionHandler.isCancelButtonVisible()) {
            cancelAction = getCancelAction();
            cancelAction.putValue(Action.NAME, actionHandler.getCancelButtonTitle());
            cancelAction.setEnabled(actionHandler.isCancelButtonEnabled());
        }

        int size = 0;
        if (okAction != null) size++;
        if (applyAction != null) size++;
        if (cancelAction != null) size++;

        Action[] actions = new Action[size];
        int index = 0;

        if (okAction != null) actions[index++] = okAction;
        if (applyAction != null) actions[index++] = applyAction;
        if (cancelAction != null) actions[index] = cancelAction;

        return actions;
    }

    protected abstract void onFinish(T context);
}
