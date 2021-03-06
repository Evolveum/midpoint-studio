package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.MidPointIcons;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
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
 * todo maybe create another refresh (non-raw) action
 * <p>
 * Created by Viliam Repan (lazyman).
 */
public class RefreshAction extends BackgroundAction {

    public static final String NOTIFICATION_KEY = "Refresh Action";

    public RefreshAction() {
        super("Refresh From Server", AllIcons.Actions.BuildLoadChanges, "Refresh From Server");
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = MidPointUtils.shouldEnableAction(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
        if (evt.getProject() == null) {
            return;
        }

        indicator.setIndeterminate(false);

        EnvironmentService em = EnvironmentService.getInstance(evt.getProject());
        Environment env = em.getSelected();

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> evt.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        AtomicInteger result = new AtomicInteger(0);

        ApplicationManager.getApplication().invokeAndWait(() -> {
            Component comp = evt.getInputEvent().getComponent();

            JComponent source;
            if (comp instanceof JComponent) {
                source = (JComponent) comp;
            } else {
                JWindow w;
                if (comp instanceof JWindow) {
                    w = (JWindow) comp;
                } else {
                    w = (JWindow) WindowManager.getInstance().suggestParentWindow(evt.getProject());
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
            MidPointUtils.publishNotification(NOTIFICATION_KEY, getTaskTitle(),
                    "No files matched for " + getTaskTitle() + " (xml)", NotificationType.WARNING);
            return;
        }

        processFiles(evt, indicator, env, toProcess);
    }

    private void processFiles(AnActionEvent evt, ProgressIndicator indicator, Environment env, List<VirtualFile> files) {
        MidPointService mm = MidPointService.getInstance(evt.getProject());

        MidPointClient client = new MidPointClient(evt.getProject(), env);

        int skipped = 0;
        int missing = 0;
        AtomicInteger reloaded = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        int current = 0;
        for (VirtualFile file : files) {
            if (isCanceled()) {
                break;
            }

            current++;
            indicator.setFraction(files.size() / current);

            List<MidPointObject> objects = new ArrayList<>();

            RunnableUtils.runWriteActionAndWait(() -> {
                MidPointUtils.forceSaveAndRefresh(evt.getProject(), file);

                List<MidPointObject> obj = MidPointUtils.parseProjectFile(file, NOTIFICATION_KEY);
                obj = ClientUtils.filterObjectTypeOnly(obj);

                objects.addAll(obj);
            });

            if (objects.isEmpty()) {
                skipped++;
                mm.printToConsole(env, RefreshAction.class, "Skipped file " + file.getPath() + " no objects found (parsed).");
                continue;
            }

            List<String> newObjects = new ArrayList<>();

            for (MidPointObject object : objects) {
                if (isCanceled()) {
                    break;
                }

                ObjectTypes type = object.getType();
                try {
                    MidPointObject newObject = client.get(type.getClassDefinition(), object.getOid(), new SearchOptions().raw(true));
                    if (newObject == null) {
                        missing++;
                        newObjects.add(object.getContent());

                        mm.printToConsole(env, RefreshAction.class, "Couldn't find object "
                                + type.getTypeQName().getLocalPart() + "(" + object.getOid() + ").");

                        continue;
                    }

                    newObjects.add(newObject.getContent());

                    reloaded.incrementAndGet();
                } catch (Exception ex) {
                    failed.incrementAndGet();
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
                    failed.incrementAndGet();

                    mm.printToConsole(env, RefreshAction.class, "Failed to write refreshed file " + file.getPath(), ex);
                }
            });
        }

        NotificationType type = missing > 0 || failed.get() > 0 || skipped > 0 ? NotificationType.WARNING : NotificationType.INFORMATION;

        StringBuilder msg = new StringBuilder();
        msg.append("Reloaded ").append(reloaded.get()).append(" objects<br/>");
        msg.append("Missing ").append(missing).append(" objects<br/>");
        msg.append("Failed to reload ").append(failed.get()).append(" objects<br/>");
        msg.append("Skipped ").append(skipped).append(" files");
        MidPointUtils.publishNotification(NOTIFICATION_KEY, "Refresh Action", msg.toString(), type);
    }
}
