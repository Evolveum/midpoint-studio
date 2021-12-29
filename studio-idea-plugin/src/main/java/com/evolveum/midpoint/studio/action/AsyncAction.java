package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.action.task.BackgroundableTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class AsyncAction<T extends BackgroundableTask> extends AnAction implements UpdateInBackground {

    public AsyncAction(@Nullable @NlsActions.ActionText String text) {
        super(text);
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = isActionEnabled(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    protected boolean isActionEnabled(AnActionEvent evt) {
        if (!MidPointUtils.isVisibleWithMidPointFacet(evt)) {
            return false;
        }

        EnvironmentService em = EnvironmentService.getInstance(evt.getProject());
        if (em.getSelected() == null) {
            return false;
        }

        return true;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() == null) {
            return;
        }

        EnvironmentService em = EnvironmentService.getInstance(e.getProject());
        Environment env = em.getSelected();

        T task = createTask(e, env);

        ProgressManager.getInstance().run(task);
    }

    protected abstract T createTask(AnActionEvent e, Environment env);
}
