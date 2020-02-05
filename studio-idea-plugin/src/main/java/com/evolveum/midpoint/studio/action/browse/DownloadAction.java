package com.evolveum.midpoint.studio.action.browse;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismSerializer;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.util.FileUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DownloadAction extends BackgroundAction {

    private static final String TASK_TITLE = "Downloading objects";

    private static final String OBJECTS_XML_PREFIX = "<objects xmlns=\"http://midpoint.evolveum.com/xml/ns/public/common/common-3\">\n";

    private static final String OBJECTS_XML_SUFFIX = "</objects>\n";

    private Environment environment;
    private boolean showOnly;
    private boolean raw;

    private ObjectTypes type;
    private ObjectQuery query;

    private List<Pair<String, ObjectTypes>> oids;

    private List<VirtualFile> createdFiles = new ArrayList<>();

    public DownloadAction(@NotNull Environment environment, @NotNull ObjectTypes type, ObjectQuery query,
                          boolean showOnly, boolean raw) {
        super(TASK_TITLE);

        this.type = type;
        this.query = query;

        this.environment = environment;
        this.showOnly = showOnly;
        this.raw = raw;
    }

    public DownloadAction(@NotNull Environment environment, @NotNull List<Pair<String, ObjectTypes>> oids, boolean showOnly, boolean raw) {
        super(TASK_TITLE);

        this.oids = oids;

        this.environment = environment;
        this.showOnly = showOnly;
        this.raw = raw;
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
        if (showOnly) {
            showOnly(evt, indicator);
        } else {
            download(evt, indicator);
        }
    }

    private void showOnly(AnActionEvent evt, ProgressIndicator indicator) {
        MidPointClient client = new MidPointClient(evt.getProject(), environment);

        PrismContext ctx = client.getPrismContext();
        PrismSerializer<String> serializer = ctx.serializerFor(PrismContext.LANG_XML);
// todo run as write action somehow
//        ApplicationManager.getApplication().invokeAndWait(() ->
//                ApplicationManager.getApplication().runWriteAction(() -> {
        BufferedWriter out = null;
        try {
            indicator.setFraction(0d);

            VirtualFile file = FileUtils.createScratchFile(evt.getProject(), environment);
            createdFiles.add(file);

            out = new BufferedWriter(new OutputStreamWriter(file.getOutputStream(this), StandardCharsets.UTF_8));
            out.write(OBJECTS_XML_PREFIX);

            if (oids != null) {
                showByOid(client, serializer, out);
            } else {
                showByQuery(client, serializer, out);
            }

            out.write(OBJECTS_XML_SUFFIX);
        } catch (Exception ex) {
            // todo handle better
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private void showByOid(MidPointClient client, PrismSerializer<String> serializer, Writer out) {
        for (Pair<String, ObjectTypes> oid : oids) {
            try {
                PrismObject object = client.get(oid.getSecond().getClassDefinition(), oid.getFirst(), new SearchOptions().raw(raw));
                String xml = serializer.serialize(object.getValue(), object.getElementName().asSingleName());

                IOUtils.write(xml, out);
            } catch (Exception ex) {
                // todo handle better
                throw new RuntimeException(ex);
            }
        }
    }

    private void showByQuery(MidPointClient client, PrismSerializer<String> serializer, Writer out) {
        // todo implement later
    }

    private void download(AnActionEvent evt, ProgressIndicator indicator) {
        MidPointClient client = new MidPointClient(evt.getProject(), environment);

        PrismContext ctx = client.getPrismContext();
        PrismSerializer<String> serializer = ctx.serializerFor(PrismContext.LANG_XML);

        BufferedWriter out = null;
        try {
            indicator.setFraction(0d);

            if (oids != null) {
                downloadByOid(client, serializer);
            } else {
                downloadByQuery(client, serializer);
            }
        } catch (Exception ex) {
            // todo handle better
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private void downloadByOid(MidPointClient client, PrismSerializer<String> serializer) {
        Project project = client.getProject();

        // todo implement later
        for (Pair<String, ObjectTypes> pair : oids) {
            try {
                PrismObject obj = client.get(pair.getSecond().getClassDefinition(), pair.getFirst(), new SearchOptions().raw(raw));
                if (obj == null) {
                    continue;
                }

                String xml = serializer.serialize(obj);

                ApplicationManager.getApplication().invokeAndWait(() ->
                        ApplicationManager.getApplication().runWriteAction(() -> {

                            Writer out = null;
                            try {
                                VirtualFile file = FileUtils.createFile(project, environment,
                                        obj.getCompileTimeClass(), obj.getOid(), MidPointUtils.getOrigFromPolyString(obj.getName()));

                                out = new BufferedWriter(
                                        new OutputStreamWriter(file.getOutputStream(DownloadAction.this), file.getCharset()));

                                IOUtils.write(xml, out);

                                createdFiles.add(file);
                            } catch (IOException ex) {
                                // todo handle exception properly
                                ex.printStackTrace();
                            } finally {
                                IOUtils.closeQuietly(out);
                            }
                        }));
            } catch (Exception ex) {
                // todo handle exception properly
                ex.printStackTrace();
            }
        }
    }

    private void downloadByQuery(MidPointClient client, PrismSerializer<String> serializer) {
        // todo implement later
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

    public List<VirtualFile> getCreatedFiles() {
        return createdFiles;
    }
}
