package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.ui.diff.DiffEditor;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SynchronizationRefreshTask extends SynchronizationTask {

    private static final Logger LOG = Logger.getInstance(SynchronizationRefreshTask.class);

    public static String TITLE = "Refresh objects (synchronization)";

    public static String NOTIFICATION_KEY = TITLE;

    private final List<SynchronizationItem> items;

    public SynchronizationRefreshTask(
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

        for (SynchronizationItem item : items) {
            switch (item.getType()) {
                case FILE:
                    processFile(counter, (SynchronizationFileItem) item);
                    break;
                case OBJECT:
                    SynchronizationObjectItem soi = (SynchronizationObjectItem) item;
                    if (items.contains(soi.getFileItem())) {
                        continue;
                    }

                    processObject(counter, soi);
                    break;
            }
        }
    }

    private void processFile(Counter counter, SynchronizationFileItem fileItem) {
        SynchronizationFileItem newFileItem = createSynchronizationFileItem(counter, fileItem.getFile());
        if (newFileItem == null) {
            return;
        }

        getSession().replaceFileItem(fileItem, newFileItem);
    }

    private void processObject(Counter counter, SynchronizationObjectItem objectItem) {
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

        SynchronizationObjectItem newObjectItem =
                createSynchronizationObjectItem(counter, objectItem.getFileItem(), local);

        getSession().replaceObjectItem(objectItem, newObjectItem);

        // close diff editors for this object
        RunnableUtils.invokeLaterIfNeeded(() -> {
            FileEditorManager fem = FileEditorManager.getInstance(getProject());
            List<DiffEditor> editors = Arrays.stream(fem.getAllEditors())
                    .filter(e -> e instanceof DiffEditor)
                    .map(e -> (DiffEditor) e)
                    .filter(e -> Objects.equals(objectItem.getId(), e.getFile().getProcessor().getId()))
                    .toList();

            editors.forEach(e -> fem.closeFile(e.getFile()));
        });
    }
}
