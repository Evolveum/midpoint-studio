package com.evolveum.midpoint.studio.action.browse;

import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.task.DownloadTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.Pair;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DownloadAction extends AsyncAction<DownloadTask> {

    private static final Logger LOG = Logger.getInstance(DownloadAction.class);

    private static final String ACTION_NAME = "Downloading objects";

    private boolean showOnly;
    private boolean raw;

    private ObjectTypes type;
    private ObjectQuery query;

    private List<Pair<String, ObjectTypes>> oids;

    private boolean overwrite;

    private boolean openAfterDownload = true;

    public DownloadAction(@NotNull ObjectTypes type, ObjectQuery query,
                          boolean showOnly, boolean raw, boolean overwrite) {
        super(ACTION_NAME);

        this.type = type;
        this.query = query;

        this.showOnly = showOnly;
        this.raw = raw;
        this.overwrite = overwrite;
    }

    public DownloadAction(@NotNull List<Pair<String, ObjectTypes>> oids, boolean showOnly, boolean raw) {
        super(ACTION_NAME);

        this.oids = oids;

        this.showOnly = showOnly;
        this.raw = raw;
    }

    public void setOpenAfterDownload(boolean openAfterDownload) {
        this.openAfterDownload = openAfterDownload;
    }

    @Override
    protected DownloadTask createTask(AnActionEvent e, Environment env) {
        DownloadTask task = new DownloadTask(e, oids, type, query, showOnly, raw, overwrite);
        task.setEnvironment(env);
        task.setOpenAfterDownload(openAfterDownload);

        return task;
    }
}
