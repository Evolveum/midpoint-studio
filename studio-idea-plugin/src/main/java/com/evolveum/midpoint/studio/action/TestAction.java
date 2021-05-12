package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.ui.ResourceWizardKt;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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

        DialogWrapper dialog = new MyDialog(project);
        dialog.show();
    }

    private static class MyDialog extends DialogWrapper {

        public MyDialog(@Nullable Project project) {
            super(project);

            setTitle("Resource Editor");
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            return ResourceWizardKt.wizard();
        }
    }
}
