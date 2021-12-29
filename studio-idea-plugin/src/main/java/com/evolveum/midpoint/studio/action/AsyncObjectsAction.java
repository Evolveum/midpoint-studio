package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.task.ObjectsBackgroundableTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class AsyncObjectsAction<T extends ObjectsBackgroundableTask> extends AsyncAction<T> {

    private List<Pair<String, ObjectTypes>> oids;

    public AsyncObjectsAction(@Nullable @NlsActions.ActionText String text) {
        super(text);
    }

    public List<Pair<String, ObjectTypes>> getOids() {
        return oids;
    }

    public void setOids(List<Pair<String, ObjectTypes>> oids) {
        this.oids = oids;
    }

    @Override
    protected boolean isActionEnabled(AnActionEvent evt) {
        if (!super.isActionEnabled(evt)) {
            return false;
        }

        return MidPointUtils.isMidpointObjectFileSelected(evt) || (oids != null && !oids.isEmpty());
    }

    @Override
    protected T createTask(AnActionEvent e, Environment env) {
        T task = createObjectsTask(e, env);
        task.setOids(getOids());
        return task;
    }

    protected abstract T createObjectsTask(AnActionEvent e, Environment env);
}
