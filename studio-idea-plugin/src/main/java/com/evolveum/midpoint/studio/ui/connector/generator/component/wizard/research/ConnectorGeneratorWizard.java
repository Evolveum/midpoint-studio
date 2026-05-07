package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research;


import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step.basic.ApplicationIdentificationStep;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step.basic.ConnectorIdentificationStep;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step.basic.DiscoverDocumentationStep;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research.step.basic.InitialStep;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ConnectorGeneratorWizard extends AbstractWizard<Step> {

    private final Project project;
    private final ConnectorGeneratorWizardData dataModel = new ConnectorGeneratorWizardData();

    private JBList<NavigationItem> stepNavigationItems;

    public ConnectorGeneratorWizard(Project project) {
        super("Connector Generator", project);
        this.project = project;
        buildSteps();
        init();
    }

    private void buildSteps() {
        var em = EnvironmentService.getInstance(project);
        var env = em.getSelected();
        var client = new MidPointClient(project, env);

        Step[] steps = {
                new InitialStep(),
                new ApplicationIdentificationStep(client, dataModel),
                new DiscoverDocumentationStep(client, dataModel),
                new ConnectorIdentificationStep(client, dataModel)
        };

        Arrays.stream(steps).forEach(this::addStep);

        NavigationItem[] items = new NavigationItem[steps.length];

        for (int i=0; i < steps.length; i++) {
            Step step = steps[i];
            items[i] = new NavigationItem(step.getComponent().getName(), "");
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

        {
            panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            panel.add(label, BorderLayout.CENTER);
        }

        stepNavigationItems.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            label.setText(value.getName() + " - " + value.getState());

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

        stepNavigationItems.setBorder(BorderFactory.createEmptyBorder());
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
        splitter.setFirstComponent(stepNavigationItems);
        splitter.setSecondComponent(super.createCenterPanel());

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

    private static class NavigationItem {
        String name;
        String state;

        public NavigationItem(String name, String state) {
            this.name = name;
            this.state = state;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
