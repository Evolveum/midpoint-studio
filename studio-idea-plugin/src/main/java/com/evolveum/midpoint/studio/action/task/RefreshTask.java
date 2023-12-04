package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.RefreshAction;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RefreshTask extends Task.Backgroundable {

    public static final String NOTIFICATION_KEY = "Refresh Action";

    private static final Logger LOG = Logger.getInstance(RefreshTask.class);

    private AnActionEvent event;

    private State state = new State();

    public RefreshTask(AnActionEvent event) {
        super(event.getProject(), "Refresh From Server", true);

        this.event = event;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        new RunnableUtils.PluginClasspathRunnable() {

            @Override
            public void runWithPluginClassLoader() {
                doRun(indicator);
            }
        }.run();
    }

    private void doRun(ProgressIndicator indicator) {
        if (getProject() == null) {
            return;
        }

        indicator.setIndeterminate(false);

        EnvironmentService em = EnvironmentService.getInstance(getProject());
        Environment env = em.getSelected();

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        AtomicInteger result = new AtomicInteger(0);

        ApplicationManager.getApplication().invokeAndWait(() -> {
            Component comp = event.getInputEvent().getComponent();

            JComponent source;
            if (comp instanceof JComponent) {
                source = (JComponent) comp;
            } else {
                JWindow w;
                if (comp instanceof JWindow) {
                    w = (JWindow) comp;
                } else {
                    w = (JWindow) WindowManager.getInstance().suggestParentWindow(getProject());
                }

                source = w.getRootPane();
            }

            int r = Messages.showConfirmationDialog(source, "Are you sure you want to reload " + toProcess.size()
                    + " file(s) from the server '" + env.getName() + "'?", "Confirm action", "Refresh", "Cancel");

            result.set(r);
        });

        if (result.get() == Messages.NO) {
            return;
        }

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, getTitle(),
                    "No files matched for " + getTitle() + " (xml)", NotificationType.WARNING);
            return;
        }

        processFiles(indicator, env, toProcess);
    }

    private void processFiles(ProgressIndicator indicator, Environment env, List<VirtualFile> files) {
        MidPointService mm = MidPointService.getInstance(getProject());

        MidPointClient client = new MidPointClient(getProject(), env);

        int current = 0;
        for (VirtualFile file : files) {
            ProgressManager.checkCanceled();

            current++;
            indicator.setFraction((double) current / files.size());

            List<MidPointObject> objects = new ArrayList<>();

            RunnableUtils.runWriteActionAndWait(() -> {
                MidPointUtils.forceSaveAndRefresh(getProject(), file);

                List<MidPointObject> obj = MidPointUtils.parseProjectFile(getProject(), file, NOTIFICATION_KEY);
                obj = ClientUtils.filterObjectTypeOnly(obj);

                objects.addAll(obj);
            });

            if (objects.isEmpty()) {
                state.incrementSkipped();
                mm.printToConsole(env, RefreshAction.class, "Skipped file " + file.getPath() + " no objects found (parsed).");
                continue;
            }

            List<String> newObjects = new ArrayList<>();

            for (MidPointObject object : objects) {
                ProgressManager.checkCanceled();

                ObjectTypes type = object.getType();
                try {
                    MidPointObject newObject = client.get(type.getClassDefinition(), object.getOid(), new SearchOptions().raw(true));

                    if (newObject == null) {
                        state.incrementMissing();
                        newObjects.add(object.getContent());

                        mm.printToConsole(env, RefreshAction.class, "Couldn't find object "
                                + type.getTypeQName().getLocalPart() + "(" + object.getOid() + ").");

                        continue;
                    }

                    newObjects.add(newObject.getContent());

                    state.incrementProcessed();
                } catch (Exception ex) {
                    state.incrementFailed();
                    newObjects.add(object.getContent());

                    mm.printToConsole(env, RefreshAction.class, "Error getting object"
                            + type.getTypeQName().getLocalPart() + "(" + object.getOid() + ")", ex);
                }
            }

            RunnableUtils.runWriteActionAndWait(() -> {
                try (Writer writer = new OutputStreamWriter(file.getOutputStream(this), file.getCharset())) {
                    if (newObjects.size() > 1) {
                        writer.write(ClientUtils.OBJECTS_XML_PREFIX);
                        writer.write('\n');
                    }

                    for (String obj : newObjects) {
                        writer.write(obj);
                    }

                    if (newObjects.size() > 1) {
                        writer.write(ClientUtils.OBJECTS_XML_SUFFIX);
                        writer.write('\n');
                    }
                } catch (IOException ex) {
                    state.incrementFailed();

                    mm.printToConsole(env, RefreshAction.class, "Failed to write refreshed file " + file.getPath(), ex);
                }
            });
        }

        NotificationType type = state.missing > 0 || state.failed > 0 || state.skipped > 0 ? NotificationType.WARNING : NotificationType.INFORMATION;
        String msg = buildFinishedNotification();

        MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, "Refresh Action", msg, type);
    }

    private String buildFinishedNotification() {
        StringBuilder msg = new StringBuilder();

        msg.append("Reloaded ").append(state.processed).append(" objects<br/>");
        msg.append("Missing ").append(state.missing).append(" objects<br/>");
        msg.append("Failed to reload ").append(state.failed).append(" objects<br/>");
        msg.append("Skipped ").append(state.skipped).append(" files");

        return msg.toString();
    }

    @Override
    public void onCancel() {
        LOG.info("Refresh action was cancelled");

        String msg = "Processed before cancel requested:<br/>" + buildFinishedNotification();
        MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, "Refresh Action Canceled", msg, NotificationType.WARNING);
    }

    private static class State {

        private int processed;

        private int missing;

        private int failed;

        private int skipped;

        void incrementProcessed() {
            processed++;
        }

        void incrementMissing() {
            missing++;
        }

        void incrementFailed() {
            failed++;
        }

        void incrementSkipped() {
            skipped++;
        }
    }
}
