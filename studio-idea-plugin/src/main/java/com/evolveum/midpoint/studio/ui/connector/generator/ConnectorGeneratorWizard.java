package com.evolveum.midpoint.studio.ui.connector.generator;

import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.ApplicationIdentificationStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.ConnectorIdentificationStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.CreateConnectorStep;
import com.evolveum.midpoint.studio.ui.connector.generator.step.basic.DiscoverDocumentationStep;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ConnectorGeneratorWizard extends AbstractWizard<Step> {

    private final Project project;
    private final ConnectorGeneratorDataModel dataModel = new ConnectorGeneratorDataModel();

    private JBList<NavigationItem> stepNavigationItems;

    public ConnectorGeneratorWizard(Project project) {
        super("Connector Generator", project);
        this.project = project;
        buildSteps();
        init();
        getOKAction().putValue(Action.NAME, "Create Connector Project");
    }

    private void buildSteps() {
        var client = new MidPointClient(project, EnvironmentService.getInstance(project).getSelected());

        Step[] steps = {
                new ApplicationIdentificationStep(client, dataModel, StepStateBadge.State.NONE),
                new DiscoverDocumentationStep(client, dataModel, StepStateBadge.State.NONE),
                new ConnectorIdentificationStep(client, dataModel, StepStateBadge.State.NONE),
                new CreateConnectorStep(client, dataModel, StepStateBadge.State.NONE)
        };

        Arrays.stream(steps).forEach(this::addStep);

        NavigationItem[] items = new NavigationItem[steps.length];

        for (int i=0; i < steps.length; i++) {
            Step step = steps[i];
            items[i] = new NavigationItem(step.getComponent().getName(), StepStateBadge.State.NONE);
        }

        stepNavigationItems = new JBList<>(items);
        stepNavigationItems.setSelectedIndex(0);
        stepNavigationItems.setEnabled(true);
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    protected @Nullable @NonNls String getHelpID() {
        return "";
    }

    @Override
    protected JComponent createCenterPanel() {
        final JBPanel<?> panel = new JBPanel<>(new BorderLayout());
        final JLabel label = new JLabel();
        final StepStateBadge stateBadge = new StepStateBadge(StepStateBadge.State.NONE);

        {
            panel.setBorder(JBUI.Borders.empty(10));
            panel.add(label, BorderLayout.CENTER);
            panel.add(stateBadge, BorderLayout.EAST);
        }

        stepNavigationItems.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            label.setText(value.getName());
            stateBadge.setState(value.getState());

            if (isSelected) {
                panel.setBackground(UIManager.getColor("List.selectionBackground"));
                label.setForeground(UIManager.getColor("List.selectionForeground"));
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            } else {
                panel.setBackground(UIManager.getColor("Panel.background"));
                label.setForeground(UIManager.getColor("Label.foreground"));
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            }

            return panel;
        });

        stepNavigationItems.setBorder(JBUI.Borders.empty());
        stepNavigationItems.setFocusable(false);
        stepNavigationItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stepNavigationItems.setFixedCellHeight(36);
        stepNavigationItems.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = stepNavigationItems.getSelectedIndex();
                if (index >= 0 && index < mySteps.size()) {
                    this.myCurrentStep = index;
                    updateStep();
                }
            }
        });

        JBSplitter splitter = new JBSplitter(false, 0.25f);
        splitter.setBorder(JBUI.Borders.empty());
        splitter.setFirstComponent(stepNavigationItems);

        JScrollPane mainPanel = ScrollPaneFactory.createScrollPane(super.createCenterPanel());
        mainPanel.setPreferredSize(new Dimension(400, 400));
        mainPanel.setBorder(JBUI.Borders.empty());
        splitter.setSecondComponent(mainPanel);

        return splitter;
    }

    @Override
    protected void doNextAction() {
        super.doNextAction();
        stepNavigationItems.setSelectedIndex(getCurrentStep());
    }

    @Override
    protected void doPreviousAction() {
        super.doPreviousAction();
        stepNavigationItems.setSelectedIndex(getCurrentStep());
    }

    public static class NavigationItem {
        String name;
        StepStateBadge.State state;

        public NavigationItem(String name, StepStateBadge.State state) {
            this.name = name;
            this.state = state;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public StepStateBadge.State getState() {
            return state;
        }

        public void setState(StepStateBadge.State state) {
            this.state = state;
        }
    }
}
