package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.studio.action.task.SimpleBackgroundableTask;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.EncryptionService;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.Expander;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SynchronizationTask extends SimpleBackgroundableTask {

    private final SynchronizationSession<?> session;

    public SynchronizationTask(
            @NotNull Project project, @NotNull String title, @NotNull String notificationKey,
            @NotNull SynchronizationSession<?> session) {

        super(project, null, title, notificationKey);

        this.session = session;
    }

    @NotNull
    public SynchronizationSession<?> getSession() {
        return session;
    }

    protected Expander createExpander() {
        EncryptionService cm = EncryptionService.getInstance(getProject());

        return new Expander(getEnvironment(), cm, getProject());
    }

    protected SynchronizationFileItem<SynchronizationObjectItem> createSynchronizationFileItem(
            Counter counter, VirtualFile file) {

        Environment env = getEnvironment();

        List<MidPointObject> objects = loadObjectsFromFile(file);
        if (objects == null) {
            counter.failed++;
            return null;
        }

        if (objects.isEmpty()) {
            counter.skipped++;
            midPointService.printToConsole(
                    env, getClass(), "Skipped file " + file.getPath() + " no objects found (parsed).");
            return null;
        }

        SynchronizationFileItem<SynchronizationObjectItem> item = new SynchronizationFileItem<>(file);

        for (MidPointObject object : objects) {
            ProgressManager.checkCanceled();

            SynchronizationObjectItem objectItem = createSynchronizationObjectItem(counter, item, object);
            if (objectItem == null) {
                continue;
            }

            item.getObjects().add(objectItem);
        }

        return item;
    }

    @Override
    protected List<MidPointObject> loadObjectsFromFile(VirtualFile file) {
        try {
            return loadObjectsFromFile(file, true);
        } catch (Exception ex) {
            midPointService.printToConsole(
                    getEnvironment(), getClass(), "Couldn't load objects from file " + file.getPath(), ex);
        }

        return null;
    }

    protected SynchronizationObjectItem createSynchronizationObjectItem(
            Counter counter, SynchronizationFileItem item, MidPointObject object) {

        Environment env = getEnvironment();
        Expander expander = createExpander();

        try {
            MidPointObject newObject = client.get(
                    object.getType().getClassDefinition(), object.getOid(), new SearchOptions().raw(true));
            if (newObject == null) {
                counter.missing++;

                midPointService.printToConsole(env, SynchronizeObjectsTask.class, "Couldn't find object "
                        + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ").");
            }

            SynchronizationObjectItem objectItem = new SynchronizationObjectItem(
                    item, object.getOid(), object.getName(), object.getType(), object, newObject);
            objectItem.initialize(expander);

            counter.processed++;

            return objectItem;
        } catch (Exception ex) {
            counter.failed++;

            midPointService.printToConsole(env, SynchronizeObjectsTask.class, "Error getting object"
                    + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ")", ex);
        }

        return null;
    }

    protected static class Counter {

        int skipped;

        int missing;

        int processed;

        int failed;
    }
}
