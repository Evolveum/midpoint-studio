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
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public abstract class MidpointWizardDialog<T> extends DialogWrapper {

    private final Project project;
//    protected final List<MidpointWizardStep> steps = new ArrayList<>();
    protected NavigationStep rootStep = new NavigationStep("Progress", StepStatus.PENDING, new JPanel());
    public NavigationStep currentStep;


    protected int currentStepIndex = 0;
    protected final T dialogWizardContext;
    private final DialogWindowActionHandler actionHandler;
    private final boolean navigationBarVisible;

    private final JPanel contentPanel = new JPanel(new BorderLayout());
    protected final JButton nextButton = new JButton("Next");
    protected final JButton prevButton = new JButton("Back");

    /**
     * Subclasses should override this to build wizard steps dynamically
     */
    protected abstract void buildSteps(T context);

    protected abstract void onFinish(T context);

    public MidpointWizardDialog(
            Project project,
            String title,
            T dialogWizardContext,
            DialogWindowActionHandler actionHandler,
            boolean navigationBarVisible
    ) {
        super(project);
        this.project = project;
        this.dialogWizardContext = dialogWizardContext;
        this.actionHandler = actionHandler;
        this.navigationBarVisible = navigationBarVisible;

        this.currentStep = rootStep;

        setTitle(title);
        init();

        if (actionHandler != null) {
            setOKButtonText(actionHandler.getOkButtonTitle());
            setCancelButtonText(actionHandler.getCancelButtonTitle());
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        buildSteps(dialogWizardContext);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(contentPanel, BorderLayout.CENTER);

        if (rootStep.hasChildren()) {
            JPanel positionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//            FIXME set enable prevButton if real does not exist prev step
//            prevButton.setEnabled(false);
            prevButton.addActionListener(e -> setCurrentStep(currentStep.getPreviousStep()));

            nextButton.addActionListener(e -> {
                setCurrentStep(currentStep.getNextStep(rootStep));
            });

            positionPanel.add(prevButton);
            positionPanel.add(nextButton);
            displayStep(currentStep);
            wrapper.add(positionPanel, BorderLayout.SOUTH);

            if (navigationBarVisible) {
                wrapper.add(createNavigationPanel(null), BorderLayout.WEST);
            }
        } else {
            wrapper.add(rootStep.getContentPanel(), BorderLayout.CENTER);
        }

        return wrapper;
    }

    private void displayStep(NavigationStep step) {
        contentPanel.removeAll();
        contentPanel.add(step.getContentPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

//        prevButton.setEnabled(currentStep.getPreviousStep() != null);
//        nextButton.setEnabled(currentStep.getNextStep() != null);
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    protected void applyFields() {
        super.applyFields();
    }

    @Override
    public void doCancelAction() {
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

    public void setCurrentStep(NavigationStep currentStep) {
        this.currentStep = currentStep;
    }

    private JComponent createNavigationPanel(List<NavigationStep> steps) {
        JBPanel<?> navigationPanel = new JBPanel<>();
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(JBUI.Borders.empty(15));
        navigationPanel.setPreferredSize(new Dimension(300, 300));

        JLabel progressLabel = new JLabel("navigationTitle");
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.BOLD, 15f));
        navigationPanel.add(progressLabel);
        navigationPanel.add(Box.createVerticalStrut(15));

//        steps.forEach(midpointWizardStep -> {
//            var navigationItem = midpointWizardStep.getNavigationItem();
//            navigationItem.getTitleLabel().addActionListener(e -> showStep(navigationItem.getStepIndex()));
//            navigationPanel.add(navigationItem);
//            navigationPanel.add(Box.createVerticalStrut(10));
//        });

        return new JBScrollPane(navigationPanel);
    }

//    public void renderSteps(JPanel panel, NavigationStep step, int indent) {
//        JPanel row = createStepRow(step, indent);
//        panel.add(row);
//
//        for (NavigationStep child : step.getChildren()) {
//            renderSteps(panel, child, indent + 20);
//        }
//    }

//    private JPanel createStepRow(NavigationStep step, int indent) {
//        JPanel row = new JPanel(new BorderLayout());
//        row.setBorder(BorderFactory.createEmptyBorder(6, indent, 6, 10));
//
//        JLabel label = new JLabel(step.getTitle());
//
//        JLabel status = new JLabel(step.getStatus().name());
//        status.setOpaque(true);
//
//        switch (step.getStatus()) {
//            case COMPLETE -> status.setBackground(new Color(76,175,80));
//            case IN_PROGRESS -> status.setBackground(new Color(0,150,136));
//            case PENDING -> status.setBackground(Color.GRAY);
//        }
//
//        if (step.getNavigationAction() != null) {
//            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//            row.addMouseListener(new java.awt.event.MouseAdapter() {
//                public void mouseClicked(java.awt.event.MouseEvent e) {
//                    step.getNavigationAction().run();
//                }
//            });
//        }
//
//        row.add(label, BorderLayout.WEST);
//        row.add(status, BorderLayout.EAST);
//
//        return row;
//    }
//
//    public JComponent buildNavigation(NavigationStep root) {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//
//        for (NavigationStep step : root.getChildren()) {
//            renderSteps(panel, step, 0);
//        }
//
//        return panel;
//    }
}
