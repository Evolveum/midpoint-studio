package com.evolveum.midpoint.studio.ui.connector.generator.component;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class StatusPanel {

    private final String ELAPSED_TEXT = "Elapsed time: %dm %ds";

    private String text;
    private String subText;
    private long elapsed;

    private JBLabel elapsedLabel;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSubText() {
        return subText;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public void updateElapsed(long elapsed) {
        this.elapsedLabel.setText(ELAPSED_TEXT.formatted(elapsed / 60, elapsed % 60));
    }

    public JBPanel<?> showAlertPanel(String title, String description, JBColor color) {
        var panel = new JBPanel<>(new CardLayout());
        panel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.CENTER, 0, 15, true, false));
        panel.setBorder(JBUI.Borders.empty(120, 20));
        panel.setBackground(UIUtil.getPanelBackground());

        var textLabel = new JBLabel(title, SwingConstants.CENTER);
        textLabel.setFont(JBUI.Fonts.label(22f).deriveFont(Font.PLAIN));
        textLabel.setForeground(color);
        panel.add(textLabel);

        var subtextJTextPanel = new JTextPane();
        subtextJTextPanel.setEditable(false);
        subtextJTextPanel.setOpaque(false);
        subtextJTextPanel.setFocusable(false);
        subtextJTextPanel.setText(description);
        subtextJTextPanel.setForeground(UIUtil.getContextHelpForeground());
        subtextJTextPanel.setBorder(new EmptyBorder(JBUI.insets(0, 10)));

        var doc = subtextJTextPanel.getStyledDocument();
        var centerAttribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerAttribute, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), centerAttribute, false);

        panel.add(subtextJTextPanel);

        return panel;
    }

    public JBPanel<?> showLoadingPanel(String title, String description, long elapsed) {
        var panel = new JBPanel<>(new CardLayout());
        panel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.CENTER, 0, 15, true, false));
        panel.setBorder(JBUI.Borders.empty(120, 20));
        panel.setBackground(UIUtil.getPanelBackground());

        var iconWrapper = new JBPanel<>(new FlowLayout(FlowLayout.CENTER));
        iconWrapper.setOpaque(false);
        iconWrapper.add(new AsyncProcessIcon("Loading"));
        panel.add(iconWrapper);

        var textLabel = new JBLabel(title, SwingConstants.CENTER);
        textLabel.setFont(JBUI.Fonts.label(22f).deriveFont(Font.PLAIN));
        textLabel.setForeground(new JBColor(new Color(70, 130, 180), new Color(70, 130, 180)));
        panel.add(textLabel);

        var subtextJTextPanel = new JTextPane();
        subtextJTextPanel.setEditable(false);
        subtextJTextPanel.setOpaque(false);
        subtextJTextPanel.setFocusable(false);
        subtextJTextPanel.setText(description);
        subtextJTextPanel.setForeground(UIUtil.getContextHelpForeground());
        subtextJTextPanel.setBorder(new EmptyBorder(JBUI.insets(0, 10)));
        panel.add(subtextJTextPanel);

        var doc = subtextJTextPanel.getStyledDocument();
        var centerAttribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerAttribute, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), centerAttribute, false);

        elapsedLabel = new JBLabel(ELAPSED_TEXT.formatted(elapsed / 60, elapsed % 60), SwingConstants.CENTER);
        elapsedLabel.setForeground(UIUtil.getInactiveTextColor());
        panel.add(elapsedLabel);

        return panel;
    }
}
