package com.evolveum.midpoint.studio.ui.dialog.wizard.navigation;

import com.evolveum.midpoint.studio.ui.dialog.wizard.MidpointStepStatus;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;

import java.awt.*;

public class NavigationItem extends JBPanel<NavigationItem> {

    private final int stepIndex;
    private final ActionLink titleLabel;

    public NavigationItem(String title, int stepIndex, MidpointStepStatus status, boolean selected) {
        this.stepIndex = stepIndex;
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        if (selected) {
            setBackground(new JBColor(new Color(224,242,241), new Color(60,63,65)));
        } else {
            setBackground(new JBColor(Color.WHITE, new Color(69,73,74)));
        }

        titleLabel = new ActionLink(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(14f));

        add(titleLabel, BorderLayout.WEST);

        NavigationStatusTag badge = NavigationStatusTag.create(status);
        if (badge != null) {
            add(badge, BorderLayout.EAST);
        }
    }

    public ActionLink getTitleLabel() {
        return titleLabel;
    }

    public int getStepIndex() {
        return stepIndex;
    }
}
