package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.task.DiffRemoteTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DiffRemoteAction extends AsyncAction<DiffRemoteTask> {

    public static final String ACTION_NAME = "Diff Remote";

    public DiffRemoteAction() {
        super(ACTION_NAME, AllIcons.Actions.Diff);
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = MidPointUtils.shouldEnableAction(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    protected DiffRemoteTask createTask(AnActionEvent e, Environment env) {
        DiffRemoteTask task = new DiffRemoteTask(e.getProject(), e::getDataContext);
        task.setEnvironment(env);

        return task;
    }
}
