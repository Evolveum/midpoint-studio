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
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
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

    private static final Logger LOG = Logger.getInstance(DownloadAction.class);

    public static final String NOTIFICATION_KEY = "Upload Action";

    private static final String TASK_TITLE = "Downloading objects";

    private static final String OBJECTS_XML_PREFIX = "<objects xmlns=\"http://midpoint.evolveum.com/xml/ns/public/common/common-3\">\n";

    private static final String OBJECTS_XML_SUFFIX = "</objects>\n";

    private Environment environment;
    private boolean showOnly;
    private boolean raw;

    private ObjectTypes type;
    private ObjectQuery query;

    private List<Pair<String, ObjectTypes>> oids;

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
        LOG.debug("Setting up midpoint client");
        MidPointClient client = new MidPointClient(evt.getProject(), environment);

        Project project = evt.getProject();

        PrismContext ctx = client.getPrismContext();
        PrismSerializer<String> serializer = ctx.serializerFor(PrismContext.LANG_XML);

        RunnableUtils.runWriteActionAndWait(() -> {
            BufferedWriter out = null;
            VirtualFile file = null;
            try {
                indicator.setFraction(0d);

                file = FileUtils.createScratchFile(project, environment);

                out = new BufferedWriter(new OutputStreamWriter(file.getOutputStream(this), StandardCharsets.UTF_8));
                out.write(OBJECTS_XML_PREFIX);

                if (oids != null) {
                    showByOid(client, serializer, out);
                } else {
                    showByQuery(client, serializer, out);
                }

                out.write(OBJECTS_XML_SUFFIX);

                openFile(project, file);
            } catch (Exception ex) {
                MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY,
                        "Exception occurred when preparing show only file " + (file != null ? file.getName() : null), ex);
            } finally {
                IOUtils.closeQuietly(out);
            }
        });
    }

    private void showByOid(MidPointClient client, PrismSerializer<String> serializer, Writer out) {
        for (Pair<String, ObjectTypes> oid : oids) {
            try {
                PrismObject object = client.get(oid.getSecond().getClassDefinition(), oid.getFirst(), new SearchOptions().raw(raw));
                String xml = serializer.serialize(object.getValue(), object.getElementName().asSingleName());

                IOUtils.write(xml, out);
            } catch (Exception ex) {
                MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY,
                        "Exception occurred when getting object " + oid.getFirst() + " ("
                                + oid.getSecond().getTypeQName().getLocalPart() + ")", ex);
            }
        }
    }

    private void showByQuery(MidPointClient client, PrismSerializer<String> serializer, Writer out) {
        // todo implement later
    }

    private void download(AnActionEvent evt, ProgressIndicator indicator) {
        LOG.debug("Setting up midpoint client");
        MidPointClient client = new MidPointClient(evt.getProject(), environment);

        PrismContext ctx = client.getPrismContext();
        PrismSerializer<String> serializer = ctx.serializerFor(PrismContext.LANG_XML);

        BufferedWriter out = null;
        try {
            indicator.setFraction(0d);

            if (oids != null) {
                downloadByOid(evt.getProject(), client, serializer);
            } else {
                downloadByQuery(client, serializer);
            }
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, "Exception occurred during download", ex);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private void downloadByOid(Project project, MidPointClient client, PrismSerializer<String> serializer) {
        List<VirtualFile> files = new ArrayList<>();

        for (Pair<String, ObjectTypes> pair : oids) {
            try {
                LOG.debug("Downloading " + pair);

                PrismObject obj = client.get(pair.getSecond().getClassDefinition(), pair.getFirst(), new SearchOptions().raw(raw));
                if (obj == null) {
                    continue;
                }

                LOG.debug("Serializing object " + obj);
                String xml = serializer.serialize(obj);

                LOG.debug("Storing file");

                RunnableUtils.runWriteActionAndWait(() -> {

                    VirtualFile file = null;
                    Writer out = null;
                    try {
                        file = FileUtils.createFile(project, environment,
                                obj.getCompileTimeClass(), obj.getOid(), MidPointUtils.getOrigFromPolyString(obj.getName()));

                        out = new BufferedWriter(
                                new OutputStreamWriter(file.getOutputStream(DownloadAction.this), file.getCharset()));

                        IOUtils.write(xml, out);

                        files.add(file);
                    } catch (IOException ex) {
                        MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY,
                                "Exception occurred when serializing object to file " + (file != null ? file.getName() : null), ex);
                    } finally {
                        IOUtils.closeQuietly(out);
                    }
                });

                LOG.debug("File saved");
            } catch (Exception ex) {
                MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY,
                        "Exception occurred when getting object " + pair.getFirst() + " ("
                                + pair.getSecond().getTypeQName().getLocalPart() + ")", ex);
            }
        }

        if (!files.isEmpty()) {
            ApplicationManager.getApplication().invokeAndWait(() -> openFile(project, files.get(0)));
        }
    }

    private void downloadByQuery(MidPointClient client, PrismSerializer<String> serializer) {
        // todo implement later
    }

    private void openFile(Project project, VirtualFile file) {
        if (file == null) {
            return;
        }

        FileEditorManager fem = FileEditorManager.getInstance(project);
        fem.openFile(file, true, true);
    }
}
