/*
 *
 * Copyright (C) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.dialog.wizard;

import com.evolveum.midpoint.studio.ui.dialog.WizardStepActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.wizard.navigation.NavigationItem;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class WizardDialog<DM> extends DialogWrapper {

    protected WizardStep<DM> rootStep;
    public WizardStep<DM> currentStep;

    protected final DM dataModel;
    private final WizardStepActionHandler actionHandler;
    private final boolean navigationBarVisible;

    private final JBPanel<?> contentPanel = new JBPanel<>(new BorderLayout());
    protected final JButton nextButton = new JButton("Next");
    protected final JButton prevButton = new JButton("Back");

    protected abstract void buildSteps(DM dataModel);

    protected abstract void onFinish(DM dataModel);

    public WizardDialog(
            Project project,
            String title,
            DM dataModel,
            WizardStep<DM> rootStep,
            WizardStepActionHandler actionHandler,
            boolean navigationBarVisible
    ) {
        super(project);
        this.dataModel = dataModel;
        this.actionHandler = actionHandler;
        this.navigationBarVisible = navigationBarVisible;
        this.rootStep = rootStep;
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
        buildSteps(dataModel);

        JBPanel<?> wrapper = new JBPanel<>(new BorderLayout());
        wrapper.add(contentPanel, BorderLayout.CENTER);

        if (rootStep.hasChildren()) {
            JBPanel<?> positionPanel = new JBPanel<>(new FlowLayout(FlowLayout.RIGHT));
            prevButton.setEnabled(currentStep.getPreviousStep(rootStep) != null);

            prevButton.addActionListener(e -> {
                changeStep(currentStep.getPreviousStep(rootStep));
            });

            nextButton.addActionListener(e -> {
                changeStep(currentStep.getNextStep(rootStep));
            });

            positionPanel.add(prevButton);
            positionPanel.add(nextButton);
            wrapper.add(positionPanel, BorderLayout.SOUTH);

            if (navigationBarVisible) {
                JBScrollPane navigationScrollPane = new JBScrollPane(createNavigationPanel(rootStep));
                navigationScrollPane.setBorder(null);
                navigationScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                navigationScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

                wrapper.add(navigationScrollPane, BorderLayout.WEST);
            }
        }

        return wrapper;
    }

    private void changeStep(WizardStep<DM> step) {
        currentStep.getContentPanel().beforeChangeAction();

        if (!currentStep.getContentPanel().disableChangeStep()) {
            setCurrentStep(step);
            contentPanel.removeAll();
            contentPanel.add(step.getContentPanel().getPanel(), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();

            prevButton.setEnabled(currentStep.getPreviousStep(rootStep) != null);
            nextButton.setEnabled(currentStep.getNextStep(rootStep) != null);
        }

        currentStep.getContentPanel().afterChangeAction();
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

    public void setCurrentStep(WizardStep<DM> currentStep) {
        this.currentStep = currentStep;
    }

    private JBPanel<?> createNavigationPanel(WizardStep<DM> rootStep) {
        JBPanel<?> navigationPanel = new JBPanel<>();
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(JBUI.Borders.empty(15));
        navigationPanel.setPreferredSize(new Dimension(300, getSize().height));

        JBLabel progressLabel = new JBLabel(rootStep.getTitle());
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.BOLD, 15f));
        navigationPanel.add(progressLabel);
        navigationPanel.add(Box.createVerticalStrut(15));

        renderNavigationItems(rootStep, navigationPanel);

        return navigationPanel;
    }

    private void renderNavigationItems(WizardStep<DM> rootStep, JBPanel<?> navigationPanel) {
        navigationPanel.removeAll();

        rootStep.getChildren().stream()
                .filter(step -> !step.isHideInNavigationMenu())
                .forEach(step -> {
                    navigationPanel.add(new NavigationItem<>(step, 0, s -> {
                        changeStep(step);
                        navigationPanel.revalidate();
                        navigationPanel.repaint();
                    }));
                    navigationPanel.add(Box.createVerticalStrut(8));
                });
    }
}
