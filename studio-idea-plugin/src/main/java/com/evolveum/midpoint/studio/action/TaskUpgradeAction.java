package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.action.task.TaskUpgradeTask;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskUpgradeAction extends AnAction {

    public static final String ACTION_NAME = "Upgrade task to activity (4.4)";

    public static final String NOTIFICATION_KEY = "Upgrade task to activity (4.4) action";

    public TaskUpgradeAction() {
        super(ACTION_NAME, ACTION_NAME, AllIcons.Actions.Annotate);
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = MidPointUtils.shouldEnableAction(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Task task = new TaskUpgradeTask(e);
        ProgressManager.getInstance().run(task);
    }
}
