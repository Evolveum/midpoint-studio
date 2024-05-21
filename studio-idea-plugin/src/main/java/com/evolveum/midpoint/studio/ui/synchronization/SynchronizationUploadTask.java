package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SynchronizationUploadTask extends SynchronizationTask {

    private static final Logger LOG = Logger.getInstance(SynchronizationRefreshTask.class);

    public static String TITLE = "Upload objects (synchronization)";

    public static String NOTIFICATION_KEY = TITLE;

    private final List<SynchronizationItem> items;

    public SynchronizationUploadTask(
            @NotNull Project project,
            @NotNull SynchronizationSession<?> session,
            @NotNull List<SynchronizationItem> items) {

        super(project, TITLE, NOTIFICATION_KEY, session);

        this.items = items;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        indicator.setIndeterminate(false);

        client.setSuppressConsole(true);
        client.setSuppressNotifications(true);

        Counter counter = new Counter();

        List<MidPointObject> objects = new ArrayList<>();

        for (SynchronizationItem item : items) {
            switch (item.getType()) {
                case FILE:
                    processFile(counter, (SynchronizationFileItem) item, objects);
                    break;
                case OBJECT:
                    SynchronizationObjectItem soi = (SynchronizationObjectItem) item;
                    if (items.contains(soi.getFileItem())) {
                        continue;
                    }

                    processObject(counter, soi, objects);
                    break;
            }
        }

        if (objects.isEmpty()) {
            return;
        }

        SynchronizationSession<?> session = getSession();

        UploadFullProcessingTask uploadTask = new UploadFullProcessingTask(
                getProject(), null, getEnvironment()) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                super.run(indicator);

                SynchronizationRefreshTask refreshTask = new SynchronizationRefreshTask(getProject(), session, items);
                refreshTask.setEnvironment(getEnvironment());
                ProgressManager.getInstance().run(refreshTask);
            }
        };
        uploadTask.setObjects(objects);
        ProgressManager.getInstance().run(uploadTask);

        // todo refresh editors
    }

    private void processFile(Counter counter, SynchronizationFileItem<SynchronizationObjectItem> fileItem, List<MidPointObject> result) {
        VirtualFile file = fileItem.getFile();
        List<MidPointObject> objects = loadObjectsFromFile(file);

        if (objects == null) {
            counter.failed++;
            return;
        }

        if (objects.isEmpty()) {
            counter.skipped++;
            midPointService.printToConsole(
                    getEnvironment(), getClass(), "Skipped file " + file.getPath() + " no objects found (parsed).");
            return;
        }

        result.addAll(objects);
    }

    private void processObject(Counter counter, SynchronizationObjectItem objectItem, List<MidPointObject> result) {
        SynchronizationFileItem fileItem = objectItem.getFileItem();
        VirtualFile file = fileItem.getFile();

        List<MidPointObject> objects = loadObjectsFromFile(file);
        if (objects == null) {
            counter.failed++;
            return;
        }

        if (objects.isEmpty()) {
            counter.skipped++;
            midPointService.printToConsole(
                    getEnvironment(), getClass(), "Skipped file " + file.getPath() + " no objects found (parsed).");
            return;
        }

        MidPointObject local = objects.stream()
                .filter(o -> Objects.equals(o.getOid(), objectItem.getOid()))
                .findFirst()
                .orElse(null);

        if (local == null) {
            return;
        }

        result.add(local);
    }
}
