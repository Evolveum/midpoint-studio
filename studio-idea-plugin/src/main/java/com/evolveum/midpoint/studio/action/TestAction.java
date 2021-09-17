package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.ui.resource.ResourceWizard;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends AnAction {

    public TestAction() {
        super("Test Action");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        if (evt.getProject() == null) {
            return;
        }

        Project project = evt.getProject();

        ResourceWizard wizard = ResourceWizard.createWizard(project);
        wizard.showAndGet();
    }
}
