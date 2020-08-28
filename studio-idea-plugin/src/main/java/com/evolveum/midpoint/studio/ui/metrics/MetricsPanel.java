package com.evolveum.midpoint.studio.ui.metrics;

import com.evolveum.midpoint.studio.ui.HeaderDecorator;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.components.BorderLayoutPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MetricsPanel extends BorderLayoutPanel {

    private JBSplitter splitter;

    public MetricsPanel() {
        initLayout();
    }

    private void initLayout() {
        JComponent toolbar = initMainToolbar();
        add(toolbar, BorderLayout.NORTH);

        OnePixelSplitter vertical = new OnePixelSplitter();
        vertical.setProportion(0.2f);
        add(vertical, BorderLayout.CENTER);

        OnePixelSplitter horizontal = new OnePixelSplitter(true);

        vertical.setFirstComponent(horizontal);
        vertical.setSecondComponent(initChartsPanel());

        horizontal.setFirstComponent(initNodesPanel());
        horizontal.setSecondComponent(initOptionsPanel());

    }

    private JPanel initNodesPanel() {
        NodesPanel panel = new NodesPanel();
        return new HeaderDecorator("Nodes", new JBScrollPane(panel));
    }

    private JPanel initOptionsPanel() {
        OptionsPanel panel = new OptionsPanel();
        return new HeaderDecorator("Metrics", new JScrollPane(panel));
    }

    private JPanel initChartsPanel() {
        ChartsPanel panel = new ChartsPanel();
        return new HeaderDecorator(" ", panel);
    }

    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction start = MidPointUtils.createAnAction("Start", AllIcons.Actions.Run_anything, e -> startClicked(e));
        group.add(start);
//
//        AnAction collapseAll = MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> main.collapseAll());
//        group.add(collapseAll);

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("MetricsMainToolbar", group, true);
        return resultsActionsToolbar.getComponent();
    }

    public void startClicked(AnActionEvent evt) {

    }
}
