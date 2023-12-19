package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.action.task.CleanupFileTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CleanupFileAction extends AsyncAction<CleanupFileTask> {

    public static final String ACTION_NAME = "Cleanup File";

    public CleanupFileAction() {
        super(ACTION_NAME);
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = MidPointUtils.isMidpointObjectFileSelected(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    public CleanupFileTask createTask(@NotNull AnActionEvent event, Environment environment) {
        return new CleanupFileTask(event, environment);
    }
}
