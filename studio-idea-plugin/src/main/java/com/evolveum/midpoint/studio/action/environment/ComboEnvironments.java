package com.evolveum.midpoint.studio.action.environment;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.JBUI;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentManager;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ComboEnvironments extends ComboBoxAction implements DumbAware {

    public static final String ACTION_ID = MidPointConstants.ACTION_ID_PREFIX + ComboEnvironments.class.getSimpleName();

    @Override
    public void update(AnActionEvent e) {
        super.update(e);

        if (e.getProject() == null) {
            return;
        }

        EnvironmentManager envManager = EnvironmentManager.getInstance(e.getProject());

        Environment env = envManager.getSelected();

        String text = env != null ? env.getName() : "None Selected";
        getTemplatePresentation().setText(text);
        e.getPresentation().setText(text);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
    }

    @Override
    public JComponent createCustomComponent(final Presentation presentation) {
        ComboBoxButton button = new ComboBoxButton(presentation) {

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(d.width, JBUI.scale(75));
                return d;
            }
        };

        NonOpaquePanel panel = new NonOpaquePanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.emptyRight(2));
        panel.add(button);

        return panel;
    }

    @NotNull
    @Override
    protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
        DefaultActionGroup group = new DefaultActionGroup();

        Project project = MidPointUtils.getCurrentProject();
        EnvironmentManager manager = EnvironmentManager.getInstance(project);

        for (Environment env : manager.getEnvironments()) {
            group.add(new SelectEnvironment(env));
        }

        group.addSeparator();

        group.add(new SelectEnvironment(null));

        group.addSeparator();

        AnAction editEnvironments = ActionManager.getInstance().getAction(EditEnvironments.ACTION_ID);
        group.add(editEnvironments);

        return group;
    }
}
