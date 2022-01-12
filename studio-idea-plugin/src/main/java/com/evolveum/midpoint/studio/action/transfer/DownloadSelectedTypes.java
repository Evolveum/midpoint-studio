package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.task.DownloadSelectedTypesTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DownloadSelectedTypes extends AsyncAction<DownloadSelectedTypesTask> {

    private static final String ACTION_NAME = "Download selected types";

    public DownloadSelectedTypes() {
        super(ACTION_NAME);
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        MidPointUtils.isVisibleWithMidPointFacet(evt);
    }

    @Override
    protected DownloadSelectedTypesTask createTask(AnActionEvent e, Environment env) {
        DownloadSelectedTypesTask task = new DownloadSelectedTypesTask(e);
        task.setEnvironment(env);

        return task;
    }
}
