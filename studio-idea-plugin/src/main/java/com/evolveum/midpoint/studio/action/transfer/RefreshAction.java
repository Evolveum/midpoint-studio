package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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

        if (evt.getProject() == null) {
            return;
        }

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> evt.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        EnvironmentManager em = EnvironmentManager.getInstance(evt.getProject());

        boolean enabled = toProcess.size() > 0 && em.getSelected() != null;
        evt.getPresentation().setEnabled(enabled);
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
        if (evt.getProject() == null) {
            return;
        }

        EnvironmentManager em = EnvironmentManager.getInstance(evt.getProject());
        Environment env = em.getSelected();

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> evt.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        AtomicInteger result = new AtomicInteger(0);

        ApplicationManager.getApplication().invokeAndWait(() -> {
            int r = Messages.showConfirmationDialog((JComponent) evt.getInputEvent().getComponent(),
                    "Are you sure you want to reload " + toProcess.size() + " file(s) from the server '" + env.getName() + "'?",
                    "Confirm action", "Refresh", "Cancel");

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
        MidPointManager mm = MidPointManager.getInstance(evt.getProject());

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
                file.refresh(false, false);

                List<MidPointObject> obj = MidPointObjectUtils.parseProjectFile(file, NOTIFICATION_KEY);
                objects.addAll(obj);
            });

            if (objects.isEmpty()) {
                skipped++;
                mm.printToConsole(RefreshAction.class, "Skipped file " + file.getPath() + " no objects found (parsed).");
                continue;
            }

            List<String> newObjects = new ArrayList<>();

            for (MidPointObject object : objects) {
                if (isCanceled()) {
                    break;
                }

                try {
                    String newObject = client.getRaw(object.getType().getClassDefinition(), object.getOid(), new SearchOptions().raw(true));
                    newObjects.add(newObject);

                    reloaded.incrementAndGet();
                } catch (ObjectNotFoundException ex) {
                    missing++;
                    newObjects.add(object.getContent());

                    mm.printToConsole(RefreshAction.class, "Couldn't find object "
                            + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ").");
                } catch (Exception ex) {
                    failed.incrementAndGet();
                    newObjects.add(object.getContent());

                    mm.printToConsole(RefreshAction.class, "Error getting object"
                            + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ")", ex);
                }
            }

            RunnableUtils.runWriteActionAndWait(() -> {
                try (Writer writer = new OutputStreamWriter(file.getOutputStream(this), file.getCharset())) {
                    if (objects.size() > 1) {
                        writer.write(MidPointObjectUtils.OBJECTS_XML_PREFIX);
                        writer.write('\n');
                    }

                    for (String obj : newObjects) {
                        writer.write(obj);
                    }

                    if (objects.size() > 1) {
                        writer.write(MidPointObjectUtils.OBJECTS_XML_SUFFIX);
                        writer.write('\n');
                    }
                } catch (IOException ex) {
                    failed.incrementAndGet();

                    mm.printToConsole(RefreshAction.class, "Failed to write refreshed file " + file.getPath(), ex);
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
