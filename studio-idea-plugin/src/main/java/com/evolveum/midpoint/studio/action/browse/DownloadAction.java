package com.evolveum.midpoint.studio.action.browse;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismSerializer;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.FileUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
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

    public void showOnly(AnActionEvent evt, ProgressIndicator indicator) {
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
                if (oids.size() > 1) {
                    out.write(MidPointObjectUtils.OBJECTS_XML_PREFIX);
                }

                if (oids != null) {
                    showByOid(client, out);
                } else {
                    showByQuery(client, out);
                }

                if (oids.size() > 1) {
                    out.write(MidPointObjectUtils.OBJECTS_XML_SUFFIX);
                }

                MidPointUtils.openFile(project, file);
            } catch (Exception ex) {
                MidPointUtils.publishExceptionNotification(environment, DownloadAction.class, NOTIFICATION_KEY,
                        "Exception occurred when preparing show only file " + (file != null ? file.getName() : null), ex);
            } finally {
                IOUtils.closeQuietly(out);
            }
        });
    }

    private void showByOid(MidPointClient client, Writer out) {
        for (Pair<String, ObjectTypes> oid : oids) {
            try {
                MidPointObject object = client.get(oid.getSecond().getClassDefinition(), oid.getFirst(), new SearchOptions().raw(raw));

                IOUtils.write(object.getContent(), out);
            } catch (Exception ex) {
                MidPointUtils.publishExceptionNotification(environment, DownloadAction.class, NOTIFICATION_KEY,
                        "Exception occurred when getting object " + oid.getFirst() + " ("
                                + oid.getSecond().getTypeQName().getLocalPart() + ")", ex);
            }
        }
    }

    private void showByQuery(MidPointClient client, Writer out) {
        // todo implement later
    }

    public void download(AnActionEvent evt, ProgressIndicator indicator) {
        LOG.debug("Setting up midpoint client");
        MidPointClient client = new MidPointClient(evt.getProject(), environment);

        PrismContext ctx = client.getPrismContext();

        BufferedWriter out = null;
        try {
            indicator.setFraction(0d);

            if (oids != null) {
                downloadByOid(evt.getProject(), client);
            } else {
                downloadByQuery(client);
            }
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(environment, DownloadAction.class, NOTIFICATION_KEY, "Exception occurred during download", ex);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private void downloadByOid(Project project, MidPointClient client) {
        List<VirtualFile> files = new ArrayList<>();

        for (Pair<String, ObjectTypes> pair : oids) {
            try {
                LOG.debug("Downloading " + pair);

                MidPointObject obj = client.get(pair.getSecond().getClassDefinition(), pair.getFirst(), new SearchOptions().raw(raw));
                if (obj == null) {
                    continue;
                }

                LOG.debug("Storing file");

                RunnableUtils.runWriteActionAndWait(() -> {

                    VirtualFile file = null;
                    Writer out = null;
                    try {
                        file = FileUtils.createFile(project, environment, obj.getType().getClassDefinition(), obj.getOid(), obj.getName());

                        out = new BufferedWriter(
                                new OutputStreamWriter(file.getOutputStream(DownloadAction.this), file.getCharset()));

                        IOUtils.write(obj.getContent(), out);

                        files.add(file);
                    } catch (IOException ex) {
                        MidPointUtils.publishExceptionNotification(environment, DownloadAction.class, NOTIFICATION_KEY,
                                "Exception occurred when serializing object to file " + (file != null ? file.getName() : null), ex);
                    } finally {
                        IOUtils.closeQuietly(out);
                    }
                });

                LOG.debug("File saved");
            } catch (Exception ex) {
                MidPointUtils.publishExceptionNotification(environment, DownloadAction.class, NOTIFICATION_KEY,
                        "Exception occurred when getting object " + pair.getFirst() + " ("
                                + pair.getSecond().getTypeQName().getLocalPart() + ")", ex);
            }
        }

        if (!files.isEmpty()) {
            ApplicationManager.getApplication().invokeAndWait(() -> MidPointUtils.openFile(project, files.get(0)));
        }
    }

    private void downloadByQuery(MidPointClient client) {
        // todo implement later
    }
}
