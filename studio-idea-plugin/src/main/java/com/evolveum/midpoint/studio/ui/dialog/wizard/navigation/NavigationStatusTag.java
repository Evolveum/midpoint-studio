package com.evolveum.midpoint.studio.ui.dialog.wizard.navigation;

import com.evolveum.midpoint.studio.ui.dialog.wizard.MidpointStepStatus;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class NavigationStatusTag extends JLabel {

    public NavigationStatusTag(String text, Color bg) {
        super(text);
        setOpaque(true);
        setBackground(bg);
        setForeground(JBColor.WHITE);
        setFont(getFont().deriveFont(Font.BOLD, 11f));
        setBorder(JBUI.Borders.empty(3, 8));
    }

    public static NavigationStatusTag create(MidpointStepStatus status) {
        return switch (status) {
            case COMPLETE ->
                    new NavigationStatusTag(MidpointStepStatus.COMPLETE.getLabel(),
                            new JBColor(new Color(76, 175, 80), new Color(76, 175, 80)));
            case IN_PROGRESS ->
                    new NavigationStatusTag(MidpointStepStatus.IN_PROGRESS.getLabel(),
                            new JBColor(new Color(0, 150, 136), new Color(0, 150, 136)));
            case FAILED ->
                    new NavigationStatusTag(MidpointStepStatus.FAILED.getLabel(),
                            new JBColor(new Color(0, 150, 136), new Color(0, 150, 136)));
            default -> null;
        };
    }
}
