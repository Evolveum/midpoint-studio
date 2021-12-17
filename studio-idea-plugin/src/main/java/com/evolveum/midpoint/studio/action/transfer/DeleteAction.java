package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.task.DeleteTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteAction extends AnAction {

    public static final String ACTION_NAME = "Delete action";

    private boolean raw;

    private List<Pair<String, ObjectTypes>> oids;

    public DeleteAction() {
        super(ACTION_NAME);
    }

    public boolean isRaw() {
        return raw;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Task task = createTask(e);
        ProgressManager.getInstance().run(task);
    }

    protected DeleteTask createTask(AnActionEvent e) {
        EnvironmentService em = EnvironmentService.getInstance(e.getProject());
        Environment env = em.getSelected();

        DeleteTask task = new DeleteTask(e, env);
        task.setRaw(raw);
        task.setOids(oids);

        return task;
    }
}
