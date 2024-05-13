package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationFileItem;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationObjectItem;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationSession;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizeObjectsTask extends SimpleBackgroundableTask {

    private static final Logger LOG = Logger.getInstance(SynchronizeObjectsTask.class);

    public static String TITLE = "Synchronize objects";

    public static String NOTIFICATION_KEY = TITLE;

    private List<VirtualFile> files;

    private SynchronizationSession session;

    public SynchronizeObjectsTask(
            @NotNull Project project, @NotNull List<VirtualFile> files, @NotNull SynchronizationSession session) {
        super(project, TITLE, NOTIFICATION_KEY);

        this.files = files;
        this.session = session;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        indicator.setIndeterminate(false);

        processFiles(indicator, files);
    }

    private void processFiles(ProgressIndicator indicator, List<VirtualFile> files) {
        client.setSuppressConsole(true);
        client.setSuppressNotifications(true);

        int skipped = 0;
        int missing = 0;
        AtomicInteger diffed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        session.open();

        Environment env = getEnvironment();

        int current = 0;
        for (VirtualFile file : files) {
            ProgressManager.checkCanceled();

            current++;
            indicator.setFraction((double) current / files.size());

            List<MidPointObject> objects;
            try {
                objects = loadObjectsFromFile(file);
            } catch (Exception ex) {
                failed.incrementAndGet();
                midPointService.printToConsole(
                        env, SynchronizeObjectsTask.class, "Couldn't load objects from file " + file.getPath(), ex);
                continue;
            }

            if (objects.isEmpty()) {
                skipped++;
                midPointService.printToConsole(
                        env, SynchronizeObjectsTask.class, "Skipped file " + file.getPath() + " no objects found (parsed).");
                continue;
            }

            SynchronizationFileItem item = new SynchronizationFileItem(file);

            for (MidPointObject object : objects) {
                ProgressManager.checkCanceled();

                try {
                    MidPointObject newObject = client.get(
                            object.getType().getClassDefinition(), object.getOid(), new SearchOptions().raw(true));
                    if (newObject == null) {
                        missing++;

                        midPointService.printToConsole(env, SynchronizeObjectsTask.class, "Couldn't find object "
                                + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ").");

                        continue;
                    }

                    SynchronizationObjectItem objectItem = new SynchronizationObjectItem(
                            item, object.getOid(), object.getName(), object.getType(), object, newObject);
                    objectItem.initialize();
                    item.getObjects().add(objectItem);

                    diffed.incrementAndGet();
                } catch (Exception ex) {
                    failed.incrementAndGet();

                    midPointService.printToConsole(env, SynchronizeObjectsTask.class, "Error getting object"
                            + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ")", ex);
                }
            }

            session.addItem(item);
        }

        // todo not very nice "dependency"
        RunnableUtils.invokeLaterIfNeeded(() -> ToolWindowManager.getInstance(getProject())
                .getToolWindow("Synchronization").show());

        NotificationType type = missing > 0 || failed.get() > 0 || skipped > 0 ?
                NotificationType.WARNING : NotificationType.INFORMATION;

        StringBuilder msg = new StringBuilder();
        msg.append("Compared ").append(diffed.get()).append(" objects<br/>");
        msg.append("Missing ").append(missing).append(" objects<br/>");
        msg.append("Failed to compare ").append(failed.get()).append(" objects<br/>");
        msg.append("Skipped ").append(skipped).append(" files");
        MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, TITLE, msg.toString(), type);
    }
}
