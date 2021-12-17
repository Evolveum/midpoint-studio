package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.task.BackgroundableTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class AsyncAction extends AnAction {

    private List<Pair<String, ObjectTypes>> oids;

    public AsyncAction(@Nullable @NlsActions.ActionText String text) {
        super(text);
    }

    public List<Pair<String, ObjectTypes>> getOids() {
        return oids;
    }

    public void setOids(List<Pair<String, ObjectTypes>> oids) {
        this.oids = oids;
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = isActionEnabled(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() == null) {
            return;
        }

        EnvironmentService em = EnvironmentService.getInstance(e.getProject());
        Environment env = em.getSelected();

        BackgroundableTask task = createTask(e, env);
        task.setOids(getOids());

        ProgressManager.getInstance().run(task);
    }

    protected boolean isActionEnabled(AnActionEvent evt) {
        if (!MidPointUtils.isVisibleWithMidPointFacet(evt)) {
            return false;
        }

        EnvironmentService em = EnvironmentService.getInstance(evt.getProject());
        if (em.getSelected() == null) {
            return false;
        }

        return MidPointUtils.isMidpointObjectFileSelected(evt) || (oids != null && !oids.isEmpty());
    }

    protected abstract BackgroundableTask createTask(AnActionEvent e, Environment env);
}
