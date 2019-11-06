package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.ui.SimpleCheckboxAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.components.BorderLayoutPanel;

import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceLogsPanel extends BorderLayoutPanel {

    private JBTextArea logs;
    private CheckboxAction logsWrapText;
    private CheckboxAction logsShowSegmentSeparators;

    public TraceLogsPanel() {
        initLayout();
    }

    private void initLayout() {
        DefaultActionGroup group = new DefaultActionGroup();
        logsWrapText = new SimpleCheckboxAction("Wrap text");
        group.add(logsWrapText);
        logsShowSegmentSeparators = new SimpleCheckboxAction("Show segment separators");
        group.add(logsShowSegmentSeparators);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceViewLogsToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);

        logs = new JBTextArea();
        add(new JBScrollPane(logs));
    }
}
