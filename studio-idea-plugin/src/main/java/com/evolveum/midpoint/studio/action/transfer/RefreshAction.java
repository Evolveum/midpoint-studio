package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.task.RefreshTask;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RefreshAction extends AnAction implements UpdateInBackground {

    public static final String NOTIFICATION_KEY = "Refresh Action";

    public static final String ACTION_TEXT = "Refresh From Server";

    public RefreshAction() {
        super(ACTION_TEXT, ACTION_TEXT, AllIcons.Actions.BuildLoadChanges);
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = MidPointUtils.shouldEnableAction(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Task task = new RefreshTask(e);
        ProgressManager.getInstance().run(task);
    }
}
