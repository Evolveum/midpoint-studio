package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.ui.MidPointProjectStructureConfigurable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ShowEnvironmentConfigurationAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        ShowSettingsUtil.getInstance().showSettingsDialog(project, MidPointProjectStructureConfigurable.class);
    }
}

