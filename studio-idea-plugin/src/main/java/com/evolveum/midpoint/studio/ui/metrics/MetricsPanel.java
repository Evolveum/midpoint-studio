package com.evolveum.midpoint.studio.ui.metrics;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
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
        add(vertical, BorderLayout.CENTER);

        OnePixelSplitter horizontal = new OnePixelSplitter(true);
        vertical.setFirstComponent(horizontal);

        JLabel asdf = new JLabel("asdf");
        horizontal.setFirstComponent(asdf);
    }

    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction start = MidPointUtils.createAnAction("Start", AllIcons.Actions.Expandall, e -> startClicked(e));
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
