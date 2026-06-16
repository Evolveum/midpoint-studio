package com.evolveum.midpoint.studio.ui.connector.generator;

import com.evolveum.midpoint.studio.ui.connector.generator.component.GenerateConnectorBadge;
import com.evolveum.midpoint.studio.ui.connector.generator.step.ConnectorGeneratorGeneralWizardStep;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public abstract class ConnectorGeneratorWizard extends AbstractWizard<Step> {

    protected final Logger log = Logger.getInstance(this.getClass());

    protected JBList<NavigationItem> stepNavigationItems;

    protected final List<ConnectorGeneratorGeneralWizardStep> myWizardStepsList = new ArrayList<>();
    protected final DefaultListModel<NavigationItem> visibleListModel = new DefaultListModel<>();
    protected final List<Integer> navIndexToStepIndexMap = new ArrayList<>();

    public ConnectorGeneratorWizard(@NotNull Project project) {
        super("Connector Generator", project);
    }

    protected abstract void buildSteps();

    @Override
    protected @Nullable @NonNls String getHelpID() {
        return "";
    }

    @Override
    protected JComponent createCenterPanel() {
        final JBPanel<?> cellWrapperPanel = new JBPanel<>(new BorderLayout());
        final JLabel label = new JLabel();
        final GenerateConnectorBadge stateBadge = new GenerateConnectorBadge(GenerateConnectorBadge.State.NONE);

        cellWrapperPanel.add(label, BorderLayout.CENTER);
        cellWrapperPanel.add(stateBadge, BorderLayout.EAST);

        stepNavigationItems.setCellRenderer((
                list,
                value,
                index,
                isSelected,
                cellHasFocus
        ) -> {
            stateBadge.setState(value.state());
            label.setText(value.name());

            if (value.isHeader()) {
                cellWrapperPanel.setBorder(JBUI.Borders.empty(12, 10, 4, 10));
                label.setFont(UIUtil.getLabelFont(UIUtil.FontSize.NORMAL).deriveFont(Font.BOLD));
                label.setForeground(UIUtil.getContextHelpForeground());
                stateBadge.setVisible(true);
            } else {
                cellWrapperPanel.setOpaque(true);
                cellWrapperPanel.setBorder(JBUI.Borders.empty(8, 22, 8, 10));
                stateBadge.setVisible(true);

                if (isSelected) {
                    label.setForeground(UIUtil.getListSelectionForeground(true));
                    label.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD, JBUI.scaleFontSize(14f)));
                } else {
                    label.setForeground(UIUtil.getLabelForeground());
                    label.setFont(UIUtil.getLabelFont().deriveFont(Font.PLAIN, JBUI.scaleFontSize(14f)));
                }
            }
            return cellWrapperPanel;
        });

        stepNavigationItems.setBorder(JBUI.Borders.empty());
        stepNavigationItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stepNavigationItems.setSelectionBackground(UIUtil.TRANSPARENT_COLOR);
        stepNavigationItems.setFixedCellHeight(-1);

        stepNavigationItems.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int listIndex = stepNavigationItems.getSelectedIndex();
                if (listIndex >= 0 && listIndex < navIndexToStepIndexMap.size()) {
                    int mappedWizardStepIndex = navIndexToStepIndexMap.get(listIndex);

                    if (mappedWizardStepIndex == -1) {
                        stepNavigationItems.setSelectedIndex(listIndex + 1);
                        return;
                    }

                    if (mappedWizardStepIndex != getCurrentStep() && mappedWizardStepIndex >= 0 && mappedWizardStepIndex < mySteps.size()) {
                        myCurrentStep = mappedWizardStepIndex;
                        updateStep();
                    }
                }
            }
        });

        JBSplitter splitter = new JBSplitter(false, 0.25f);
        splitter.setBorder(JBUI.Borders.empty());
        splitter.setFirstComponent(stepNavigationItems);

        JScrollPane mainPanel = ScrollPaneFactory.createScrollPane(super.createCenterPanel());
        mainPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        mainPanel.setBorder(JBUI.Borders.empty());
        splitter.setSecondComponent(mainPanel);

        return splitter;
    }

    @Override
    public void doNextAction() {
        super.doNextAction();
        updateNavigationMenuByLiveStates();
    }

    @Override
    protected void doPreviousAction() {
        super.doPreviousAction();
        stepNavigationItems.setSelectedIndex(getCurrentStep());
    }

    @Override
    protected boolean canGoNext() {
        return myWizardStepsList.get(getCurrentStep()).isCanGoNext();
    }

    @Override
    protected boolean canFinish() {
        return myWizardStepsList.get(getCurrentStep()).isCanGoNext();
    }

    public void updateNavigationMenuByLiveStates() {
        visibleListModel.clear();
        navIndexToStepIndexMap.clear();

        IntStream.range(0, myWizardStepsList.size()).forEach(
                i -> addStepIfStateNotNone(
                        i,
                        myWizardStepsList.get(i).getComponent().getName(),
                        myWizardStepsList.get(i).isHeader()
                ));

        synchronizeMenuHighlight();
    }

    private void addStepIfStateNotNone(int stepIndex, String stepDisplayName, boolean isHeader) {
        Step step = myWizardStepsList.get(stepIndex);

        if (step instanceof ConnectorGeneratorGeneralWizardStep generatorStep) {
            GenerateConnectorBadge.State currentState = generatorStep.getState();

            if (!currentState.equals(GenerateConnectorBadge.State.NONE)) {
                visibleListModel.addElement(new NavigationItem(stepDisplayName, currentState, isHeader));
                navIndexToStepIndexMap.add(stepIndex);
            }
        }
    }

    private void synchronizeMenuHighlight() {
        IntStream.range(0, navIndexToStepIndexMap.size())
                .filter(i -> navIndexToStepIndexMap.get(i) == getCurrentStep())
                .findFirst()
                .ifPresent(stepNavigationItems::setSelectedIndex);
    }
}