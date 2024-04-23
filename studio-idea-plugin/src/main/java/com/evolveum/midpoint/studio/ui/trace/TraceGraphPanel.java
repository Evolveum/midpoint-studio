package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.TraceService;
import com.evolveum.midpoint.studio.ui.HeaderDecorator;
import com.evolveum.midpoint.studio.ui.UiAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pavol Mederly
 */
public class TraceGraphPanel extends BorderLayoutPanel {

    private final TraceService traceManager;

    private JCheckBox showFocusCurrent;
    private JCheckBox showProjectionCurrent;
    private JCheckBox useSelectedNodes;
    private JCheckBox skipDisabledNodes;

    public TraceGraphPanel(Project project) {
        this.traceManager = TraceService.getInstance(project);


        initLayout();
    }

    private void initLayout() {
        createToolbar();

        JPanel root = new BorderLayoutPanel();

        createClockworkGraphPanel(root);
        createNodeSelectionPanel(root);

        add(new JBScrollPane(root));
    }

    private void createClockworkGraphPanel(JPanel root) {
        JPanel panel = createBoxLayoutPanel();
        panel.setBorder(JBUI.Borders.empty(5));

        panel.add(showFocusCurrent = new JCheckBox("Show focus current object"));
        panel.add(showProjectionCurrent = new JCheckBox("Show projection current object"));

        root.add(new HeaderDecorator("Clockwork graph options", panel), BorderLayout.NORTH);
    }

    private void createNodeSelectionPanel(JPanel root) {
        JPanel panel = createBoxLayoutPanel();
        panel.setBorder(JBUI.Borders.empty(5));

        panel.add(useSelectedNodes = new JCheckBox("Use selected nodes"));
        panel.add(skipDisabledNodes = new JCheckBox("Skip disabled nodes"));

        root.add(new HeaderDecorator("Node selection", panel), BorderLayout.CENTER);
    }

    private JPanel createBoxLayoutPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(JBUI.Borders.emptyLeft(5));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private void createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction create = new UiAction("Create", AllIcons.General.GreenCheckmark, e -> applyPerformed());
        group.add(create);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceGraphToolbar", group, true);
        toolbar.setTargetComponent(this);
        add(toolbar.getComponent(), BorderLayout.NORTH);
    }

    private void applyPerformed() {
    }

}
