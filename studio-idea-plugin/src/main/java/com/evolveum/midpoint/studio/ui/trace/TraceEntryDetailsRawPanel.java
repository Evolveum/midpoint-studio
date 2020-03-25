package com.evolveum.midpoint.studio.ui.trace;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.components.BorderLayoutPanel;

import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceEntryDetailsRawPanel extends BorderLayoutPanel {

    private JBTextArea text;

    public TraceEntryDetailsRawPanel() {
        initLayout();
    }

    private void initLayout() {
        text = new JBTextArea();

        add(new JBScrollPane(text), BorderLayout.CENTER);
    }
}
