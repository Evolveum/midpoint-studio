package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.client.SearchResult;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.SearchOptions;
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
public class DownloadTask extends SimpleBackgroundableTask {

    private static final Logger LOG = Logger.getInstance(DownloadTask.class);

    public static String TITLE = "Download task";

    public static String NOTIFICATION_KEY = TITLE;

    private boolean showOnly;
    private boolean raw;

    private ObjectTypes type;
    private ObjectQuery query;

    private List<Pair<String, ObjectTypes>> oids;

    private boolean overwrite;

    private boolean openAfterDownload = true;

    public DownloadTask(@NotNull AnActionEvent event, List<Pair<String, ObjectTypes>> oids, ObjectTypes type, ObjectQuery query, boolean showOnly, boolean raw, boolean overwrite) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);

        this.oids = oids;
        this.type = type;
        this.query = query;

        this.showOnly = showOnly;
        this.raw = raw;
        this.overwrite = overwrite;
    }

    public void setOpenAfterDownload(boolean openAfterDownload) {
        this.openAfterDownload = openAfterDownload;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        if (showOnly) {
            showOnly(indicator);
        } else {
            download(indicator);
        }
    }

    public void showOnly(ProgressIndicator indicator) {
        indicator.setIndeterminate(true);

        Project project = getProject();
        Environment environment = getEnvironment();

        RunnableUtils.runWriteActionAndWait(() -> {
            BufferedWriter out = null;
            VirtualFile file = null;
            try {
                indicator.setFraction(0d);

                file = FileUtils.createScratchFile(project, environment);

                out = new BufferedWriter(new OutputStreamWriter(file.getOutputStream(this), StandardCharsets.UTF_8));
                if (oids.size() > 1) {
                    out.write(ClientUtils.OBJECTS_XML_PREFIX);
                }

                if (oids != null) {
                    showByOid(out);
                } else {
                    showByQuery(out);
                }

                if (oids.size() > 1) {
                    out.write(ClientUtils.OBJECTS_XML_SUFFIX);
                }

                MidPointUtils.openFile(project, file);
            } catch (Exception ex) {
                MidPointUtils.publishExceptionNotification(project, environment, DownloadTask.class, NOTIFICATION_KEY,
                        "Exception occurred when preparing show only file " + (file != null ? file.getName() : null), ex);
            } finally {
                IOUtils.closeQuietly(out);
            }
        });
    }

    private void showByOid(Writer out) {
        for (Pair<String, ObjectTypes> oid : oids) {
            try {
                MidPointObject object = client.get(oid.getSecond().getClassDefinition(), oid.getFirst(), new SearchOptions().raw(raw));
                if (object == null) {
                    continue;
                }

                String content = oids.size() > 1 ? MidPointUtils.updateObjectRootElementToObject(object.getContent()) : object.getContent();

                IOUtils.write(content, out);
            } catch (Exception ex) {
                MidPointUtils.publishExceptionNotification(getProject(), getEnvironment(), DownloadTask.class, NOTIFICATION_KEY,
                        "Exception occurred when getting object " + oid.getFirst() + " ("
                                + oid.getSecond().getTypeQName().getLocalPart() + ")", ex);
            }
        }
    }

    private void showByQuery(Writer out) {
        // todo implement later
    }

    public void download(ProgressIndicator indicator) {
        indicator.setIndeterminate(true);

        BufferedWriter out = null;
        try {
            indicator.setFraction(0d);

            if (oids != null) {
                downloadByOid();
            } else {
                downloadByQuery();
            }
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(getProject(), getEnvironment(), DownloadTask.class,
                    NOTIFICATION_KEY, "Exception occurred during download", ex);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private void downloadByOid() {
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

                    VirtualFile file = saveFile(obj);
                    if (file != null) {
                        files.add(file);
                    }
                });

                LOG.debug("File saved");
            } catch (Exception ex) {
                MidPointUtils.publishExceptionNotification(getProject(), getEnvironment(), DownloadTask.class, NOTIFICATION_KEY,
                        "Exception occurred when getting object " + pair.getFirst() + " ("
                                + pair.getSecond().getTypeQName().getLocalPart() + ")", ex);
            }
        }

        if (!files.isEmpty() && openAfterDownload) {
            ApplicationManager.getApplication().invokeAndWait(() -> MidPointUtils.openFile(getProject(), files.get(0)));
        }
    }

    private void downloadByQuery() {
        List<VirtualFile> files = new ArrayList<>();

        try {
            SearchResult result = client.search(type.getClassDefinition(), query, raw);
            if (result == null || result.getObjects() == null) {
                return;
            }

            LOG.debug("Storing file");

            RunnableUtils.runWriteActionAndWait(() -> {

                for (MidPointObject obj : result.getObjects()) {
                    VirtualFile file = saveFile(obj);
                    if (file != null) {
                        files.add(file);
                    }
                }
            });

            LOG.debug("Files saved");
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(getProject(), getEnvironment(), DownloadTask.class, NOTIFICATION_KEY,
                    "Exception occurred when searching for " + type.getValue(), ex);
        }

        if (!files.isEmpty() && openAfterDownload) {
            ApplicationManager.getApplication().invokeAndWait(() -> MidPointUtils.openFile(getProject(), files.get(0)));
        }
    }

    private VirtualFile saveFile(MidPointObject obj) {
        VirtualFile file = null;
        Writer out = null;
        try {
            file = FileUtils.createFile(getProject(), getEnvironment(), obj.getType().getClassDefinition(), obj.getOid(), obj.getName(), overwrite);

            out = new BufferedWriter(
                    new OutputStreamWriter(file.getOutputStream(DownloadTask.this), file.getCharset()));

            IOUtils.write(obj.getContent(), out);

            return file;
        } catch (IOException ex) {
            MidPointUtils.publishExceptionNotification(getProject(), getEnvironment(), DownloadTask.class, NOTIFICATION_KEY,
                    "Exception occurred when serializing object to file " + (file != null ? file.getName() : null), ex);
        } finally {
            IOUtils.closeQuietly(out);
        }

        return null;
    }
}
