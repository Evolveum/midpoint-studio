package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.action.task.CleanupFileTask;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CleanupFileAction extends AnAction {

    public static final String ACTION_NAME = "Cleanup File";

    public CleanupFileAction() {
        super(ACTION_NAME);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = MidPointUtils.isMidpointObjectFileSelected(evt);
        SwingUtilities.invokeLater(() -> evt.getPresentation().setEnabled(enabled));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Task task = new CleanupFileTask(e);
        ProgressManager.getInstance().run(task);
    }
}
