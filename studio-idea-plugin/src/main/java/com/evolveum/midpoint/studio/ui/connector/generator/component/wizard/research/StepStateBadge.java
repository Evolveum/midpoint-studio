package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.research;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class StepStateBadge extends JLabel {

    public enum State {
        COMPLETE(
                "Complete",
                new JBColor(new Color(0xDDF4E4), new Color(0x35523C)),
                new JBColor(new Color(0x8BC79A), new Color(0x5FA86E)),
                new JBColor(new Color(0x1E4620), new Color(0xDDF4E4))
        ),
        IN_PROGRESS(
                "In progress",
                new JBColor(new Color(0xD7EEF2), new Color(0x2F4F58)),
                new JBColor(new Color(0x8BC3CC), new Color(0x5FA8B5)),
                new JBColor(new Color(0x0F3B46), new Color(0xD7EEF2))
        ),
        EDITED(
                "Edited",
                new JBColor(new Color(0xF4E7D6), new Color(0x5A4632)),
                new JBColor(new Color(0xD2B48C), new Color(0xB08968)),
                new JBColor(new Color(0x5C3B12), new Color(0xFFEBD2))
        ),
        FIXING(
                "Fixing",
                new JBColor(new Color(0xFCE2E2), new Color(0x5C3232)),
                new JBColor(new Color(0xE5A1A1), new Color(0xC16B6B)),
                new JBColor(new Color(0x5A1E1E), new Color(0xFFECEC))
        ),
        NONE(
                "None",
                new JBColor(new Color(0xECECEC), new Color(0x3C3F41)),
                new JBColor(new Color(0xD0D0D0), new Color(0x5A5D5F)),
                new JBColor(new Color(0x666666), new Color(0xBBBBBB))
        );

        final String value;
        final Color background;
        final Color border;
        final Color foreground;

        State(String value, Color background, Color border, Color foreground) {
            this.value = value;
            this.background = background;
            this.border = border;
            this.foreground = foreground;
        }
    }

    private State state;

    public StepStateBadge(State state) {
        super(state.value);
        this.state = state;
        setOpaque(false);
        setFont(getFont().deriveFont(Font.BOLD, JBUI.scaleFontSize(14f)));
        setBorder(JBUI.Borders.empty(6, 12));
        updateColors();
    }

    public void setState(State state) {
        this.state = state;
        setText(state.value);
        updateColors();
        repaint();
    }

    private void updateColors() {
        setForeground(state.foreground);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        try {
            int arc = JBUI.scale(12);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(state.background);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(state.border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            super.paintComponent(g2);
        } finally {
            g2.dispose();
        }
    }
}