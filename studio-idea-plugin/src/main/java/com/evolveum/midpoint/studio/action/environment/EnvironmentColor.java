package com.evolveum.midpoint.studio.action.environment;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.JBUI;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentManager;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnvironmentColor extends DumbAwareAction implements CustomComponentAction {

    private static final int WIDTH = 150;
    private static final int HEIGHT = 16;

    public EnvironmentColor() {
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

    }

    @Override
    public JComponent createCustomComponent(Presentation presentation, String place) {
        JPanel panel = new JPanel() {

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(d.width, JBUI.scale(EnvironmentColor.WIDTH));
                d.height = JBUI.scale(EnvironmentColor.HEIGHT);
                return d;
            }
        };
        panel.setBorder(JBUI.Borders.emptyRight(2));
        panel.setOpaque(true);

        return panel;
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);

        if (e.getPresentation() == null || e.getProject() == null) {
            return;
        }

        JComponent panel = ObjectUtils.tryCast(e.getPresentation()
                .getClientProperty(CustomComponentAction.COMPONENT_KEY), JComponent.class);

        if (panel == null) {
            return;
        }

        EnvironmentManager envManager = EnvironmentManager.getInstance(e.getProject());
        Environment env = envManager.getSelected();

        if (env == null || env.getColor() == null) {
            panel.setOpaque(false);
            return;
        }

        panel.setOpaque(true);

        panel.setBackground(env.getColor().asAwtColor());
    }
}
