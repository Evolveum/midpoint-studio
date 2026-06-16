package com.evolveum.midpoint.studio.ui.connector.generator.component;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AiAlertPanel extends JPanel {

    private final JBColor COLOR_AI_ALERT =
            new JBColor(new Color(221, 208, 249), new Color(221, 208, 249));

    private final JBColor COLOR_AI_TEXT =
            new JBColor(new Color(37, 8, 93), new Color(37, 8, 93));

    private final int arcSize;

    public AiAlertPanel(LayoutManager layout, int arcSize, String text, String subtext) {
        super(layout);
        this.arcSize = arcSize;
        setOpaque(false);
        setBackground(COLOR_AI_ALERT);
        setBorder(new RoundedLineBorder(COLOR_AI_TEXT, arcSize, 2));

        Icon coloredIcon = IconUtil.colorize(AllIcons.Diff.MagicResolve, COLOR_AI_TEXT);
        var aiAlertIcon = new JBLabel();
        Icon largeIcon = IconUtil.scale(coloredIcon, aiAlertIcon, 1.7f);
        aiAlertIcon.setIcon(largeIcon);
        aiAlertIcon.setHorizontalAlignment(SwingConstants.CENTER);
        aiAlertIcon.setPreferredSize(new Dimension(50, 50));
        aiAlertIcon.setOpaque(false);
        add(aiAlertIcon, BorderLayout.WEST);

        JPanel contentPanel = new JPanel(new GridLayout(2, 1));
        contentPanel.setOpaque(false);

        var textJBLabel = new JBLabel(text);
        textJBLabel.setFont(JBFont.label().asBold().deriveFont(JBUI.scale(14f)));
        textJBLabel.setForeground(COLOR_AI_TEXT);
        textJBLabel.setBorder(new EmptyBorder(JBUI.insets(0, 10)));
        contentPanel.add(textJBLabel);

        var subtextJTextPanel = new JTextPane();
        subtextJTextPanel.setEditable(false);
        subtextJTextPanel.setOpaque(false);
        subtextJTextPanel.setForeground(COLOR_AI_TEXT);
        subtextJTextPanel.setFocusable(false);
        subtextJTextPanel.setText(subtext);
        subtextJTextPanel.setBorder(new EmptyBorder(JBUI.insets(0, 10)));
        contentPanel.add(subtextJTextPanel);

        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        int scaledArc = JBUI.scale(arcSize);

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), scaledArc, scaledArc);
        g2.dispose();
    }
}
