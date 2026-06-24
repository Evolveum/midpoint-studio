package com.evolveum.midpoint.studio.ui.connector.generator.component;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class GenerateConnectorBadge extends JLabel {

    public interface BadgeType {
        String getName();

        Color getBackground();

        Color getBorder();

        Color getForeground();
    }

    public record BadgeStyle(
            String name,
            Color background,
            Color border,
            Color foreground
    ) {
    }

    public enum State implements BadgeType {
        COMPLETE(new BadgeStyle(
                "Complete",
                new JBColor(new Color(0xDDF4E4), new Color(0x35523C)),
                new JBColor(new Color(0x8BC79A), new Color(0x5FA86E)),
                new JBColor(new Color(0x1E4620), new Color(0xDDF4E4))
        )),
        IN_PROGRESS(new BadgeStyle(
                "In progress",
                new JBColor(new Color(0xD7EEF2), new Color(0x2F4F58)),
                new JBColor(new Color(0x8BC3CC), new Color(0x5FA8B5)),
                new JBColor(new Color(0x0F3B46), new Color(0xD7EEF2))
        )),
        EDITED(new BadgeStyle(
                "Edited",
                new JBColor(new Color(0xF4E7D6), new Color(0x5A4632)),
                new JBColor(new Color(0xD2B48C), new Color(0xB08968)),
                new JBColor(new Color(0x5C3B12), new Color(0xFFEBD2))
        )),
        FIXING(new BadgeStyle(
                "Fixing",
                new JBColor(new Color(0xFCE2E2), new Color(0x5C3232)),
                new JBColor(new Color(0xE5A1A1), new Color(0xC16B6B)),
                new JBColor(new Color(0x5A1E1E), new Color(0xFFECEC))
        )),
        NONE(new BadgeStyle(
                "None",
                new JBColor(new Color(0xECECEC), new Color(0x3C3F41)),
                new JBColor(new Color(0xD0D0D0), new Color(0x5A5D5F)),
                new JBColor(new Color(0x666666), new Color(0xBBBBBB))
        ));

        private final BadgeStyle style;

        State(BadgeStyle style) {
            this.style = style;
        }

        @Override
        public String getName() {
            return style.name();
        }

        @Override
        public Color getBackground() {
            return style.background();
        }

        @Override
        public Color getBorder() {
            return style.border();
        }

        @Override
        public Color getForeground() {
            return style.foreground();
        }
    }

    public enum Recommended implements BadgeType {
        RECOMMENDED(new BadgeStyle(
                "Recommended",
                new JBColor(new Color(0xDDF4E4), new Color(0x35523C)),
                new JBColor(new Color(0x8BC79A), new Color(0x5FA86E)),
                new JBColor(new Color(0x1E4620), new Color(0xDDF4E4))
        ));

        private final BadgeStyle style;

        Recommended(BadgeStyle style) {
            this.style = style;
        }

        @Override
        public String getName() {
            return style.name();
        }

        @Override
        public Color getBackground() {
            return style.background();
        }

        @Override
        public Color getBorder() {
            return style.border();
        }

        @Override
        public Color getForeground() {
            return style.foreground();
        }
    }

    public enum AiTag implements BadgeType {
        AI_TAG(new BadgeStyle(
                "AI",
                new JBColor(new Color(0xDDD0F9), new Color(0x4A3A6B)),
                new JBColor(new Color(0xDDD0F9), new Color(0x8A6FD1)),
                new JBColor(new Color(0x38265C), new Color(0xDDD0F9))
        ));

        private final BadgeStyle style;

        AiTag(BadgeStyle style) {
            this.style = style;
        }

        @Override
        public String getName() {
            return style.name();
        }

        @Override
        public Color getBackground() {
            return style.background();
        }

        @Override
        public Color getBorder() {
            return style.border();
        }

        @Override
        public Color getForeground() {
            return style.foreground();
        }
    }

    private BadgeType badge;

    public GenerateConnectorBadge(BadgeType badge) {
        super(badge.getName());

        this.badge = badge;

        setOpaque(false);
        setFont(getFont().deriveFont(Font.BOLD, JBUI.scaleFontSize(12f)));
        setBorder(JBUI.Borders.empty(6, 12));
        setForeground(badge.getForeground());
    }

    public void setBadge(BadgeType badge) {
        this.badge = badge;

        setText(badge.getName());
        setForeground(badge.getForeground());

        repaint();
    }

    public BadgeType getBadge() {
        return badge;
    }

    public void setState(State state) {
        setBadge(state);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        try {
            int arc = JBUI.scale(12);

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (badge != null) {
                g2.setColor(badge.getBackground());
                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        arc,
                        arc
                );

                g2.setColor(badge.getBorder());
                g2.drawRoundRect(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - 1,
                        arc,
                        arc
                );
            }

            super.paintComponent(g2);
        } finally {
            g2.dispose();
        }
    }
}