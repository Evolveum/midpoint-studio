package com.evolveum.midpoint.studio.action.browse;

import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.RestObjectManager;
import com.evolveum.midpoint.studio.util.Pair;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DownloadAction extends BackgroundAction {

    private static final String TASK_TITLE = "Downloading objects";

    private boolean showOnly;

    private ObjectTypes type;
    private ObjectQuery query;

    private List<Pair<String, ObjectTypes>> oids;

    public DownloadAction(@NotNull ObjectTypes type, ObjectQuery query, boolean showOnly) {
        super(TASK_TITLE);

        this.type = type;
        this.query = query;

        this.showOnly = showOnly;
    }

    public DownloadAction(@NotNull List<Pair<String, ObjectTypes>> oids, boolean showOnly) {
        super(TASK_TITLE);

        this.oids = oids;

        this.showOnly = showOnly;
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
        if (oids != null) {
            searchByOids(evt, indicator);
        } else {
            searchByQuery(evt, indicator);
        }
    }

    private void searchByOids(AnActionEvent evt, ProgressIndicator indicator) {
        // todo implement
    }

    private void searchByQuery(AnActionEvent evt, ProgressIndicator indicator) {
        // todo implement
    }

    private void mess() {
//        RestObjectManager rest = RestObjectManager.getInstance(evt.getProject());
//
//        ObjectQuery objectQuery = null;
//            todo fix
//            if (table.getRowCount() == table.getSelectedRowCount() || table.getSelectedRowCount() == 0) {
//                // return all
//                objectQuery = buildQuery(evt.getProject());
//            } else {
//                // return only selected objects
//                List<String> oids = getSelectedRowsOids();
//
//                PrismContext ctx = rest.getPrismContext();
//                QueryFactory qf = ctx.queryFactory();
//
//                InOidFilter inOidFilter = qf.createInOid(oids);
//
//                ItemPath path = ctx.path(ObjectType.F_NAME);
//                ObjectPaging paging = qf.createPaging(this.paging.getFrom(), this.paging.getPageSize(),
//                        path, OrderDirection.ASCENDING);
//
//                objectQuery = qf.createQuery(inOidFilter, paging);
//            }
//
//        ObjectTypes objectTypes = objectType.getSelected();
//        VirtualFile[] files = rest.download(objectTypes.getClassDefinition(), objectQuery,
//                new DownloadOptions().showOnly(showOnly).raw(rawDownload));
//
//        if (files != null && files.length == 1) {
//            FileEditorManager fem = FileEditorManager.getInstance(evt.getProject());
//            fem.openFile(files[0], true, true);
//        }
    }
}
