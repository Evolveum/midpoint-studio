package com.evolveum.midpoint.studio.ui.dialog.wizard.navigation;

import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class NavigationGroup extends JBPanel<NavigationGroup> {

    public NavigationGroup(String title) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(JBUI.Borders.emptyLeft(15));

        JLabel groupLabel = new JLabel(title);
        groupLabel.setFont(groupLabel.getFont().deriveFont(Font.BOLD, 13f));
        groupLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        add(groupLabel);
        add(Box.createVerticalStrut(5));
    }

    public void addItem(JComponent item) {
        add(item);
        add(Box.createVerticalStrut(8));
    }
}
