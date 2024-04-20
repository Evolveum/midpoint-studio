package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.ui.diff.SynchronizationFileItem;
import com.evolveum.midpoint.studio.ui.diff.SynchronizationManager;
import com.evolveum.midpoint.studio.ui.diff.SynchronizationObjectItem;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizeObjectsTask extends SimpleBackgroundableTask {

    private static final Logger LOG = Logger.getInstance(DiffRemoteTask.class);

    public static String TITLE = "Synchronize objects";

    public static String NOTIFICATION_KEY = TITLE;

    private List<VirtualFile> files;

    public SynchronizeObjectsTask(@NotNull Project project, @NotNull List<VirtualFile> files) {
        super(project, TITLE, NOTIFICATION_KEY);

        this.files = files;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        indicator.setIndeterminate(false);

        processFiles(indicator, files);
    }

    private void processFiles(ProgressIndicator indicator, List<VirtualFile> files) {
        SynchronizationManager sm = SynchronizationManager.get(getProject());

        int skipped = 0;
        int missing = 0;
        AtomicInteger diffed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        try {
            sm.start();

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
                    midPointService.printToConsole(env, DiffRemoteTask.class, "Couldn't load objects from file " + file.getPath(), ex);
                    continue;
                }

                if (objects.isEmpty()) {
                    skipped++;
                    midPointService.printToConsole(env, DiffRemoteTask.class, "Skipped file " + file.getPath() + " no objects found (parsed).");
                    continue;
                }

                List<SynchronizationFileItem> items = new ArrayList<>();
                SynchronizationFileItem item = new SynchronizationFileItem(file, new ArrayList<>());
                items.add(item);

                for (MidPointObject object : objects) {
                    ProgressManager.checkCanceled();

                    try {
                        MidPointObject newObject = client.get(object.getType().getClassDefinition(), object.getOid(), new SearchOptions().raw(true));
                        if (newObject == null) {
                            missing++;

                            midPointService.printToConsole(env, DiffRemoteTask.class, "Couldn't find object "
                                    + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ").");

                            continue;
                        }

                        item.objects().add(
                                new SynchronizationObjectItem(
                                        object.getOid(), object.getName(), object.getType(), object, newObject));

                        diffed.incrementAndGet();
                    } catch (Exception ex) {
                        failed.incrementAndGet();

                        midPointService.printToConsole(env, DiffRemoteTask.class, "Error getting object"
                                + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ")", ex);
                    }
                }

                sm.add(items);
            }
        } finally {
            sm.finish();
        }

        NotificationType type = missing > 0 || failed.get() > 0 || skipped > 0 ? NotificationType.WARNING : NotificationType.INFORMATION;

        StringBuilder msg = new StringBuilder();
        msg.append("Compared ").append(diffed.get()).append(" objects<br/>");
        msg.append("Missing ").append(missing).append(" objects<br/>");
        msg.append("Failed to compare ").append(failed.get()).append(" objects<br/>");
        msg.append("Skipped ").append(skipped).append(" files");
        MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, TITLE, msg.toString(), type);
    }
}
