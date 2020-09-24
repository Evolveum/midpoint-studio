package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.CredentialsService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CredentialsPanel extends BorderLayoutPanel {

    public CredentialsPanel(Project project) {
        initLayout(project);
    }

    private void initLayout(Project project) {
        JBTabbedPane tabbedPane = new JBTabbedPane();
        tabbedPane.setTabComponentInsets(new Insets(0, 0, 0, 0));

        add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addTab("Properties", new EncryptedPropertiesPanel(project));
        tabbedPane.addTab("Environments", new JPanel());
    }

    public AnAction[] createConsoleActions() {
        return new AnAction[]{
                new AnAction("Refresh", "Refresh", AllIcons.Actions.Refresh) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        CredentialsService manager = CredentialsService.getInstance(e.getProject());
                        manager.refresh();

                        refreshUi();
                    }
                }
        };
    }

    private void refreshUi() {
        // todo implement
    }
}
