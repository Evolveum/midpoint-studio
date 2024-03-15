package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.impl.xml.LocationType;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DiffRemoteTask extends DiffTask {

    private static final Logger LOG = Logger.getInstance(DiffRemoteTask.class);

    public static String TITLE = "Diff remote task";

    public static String NOTIFICATION_KEY = TITLE;

    public DiffRemoteTask(@NotNull AnActionEvent event) {
        super(event, TITLE, NOTIFICATION_KEY);
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        indicator.setIndeterminate(false);

        VirtualFile[] selectedFiles = UIUtil.invokeAndWaitIfNeeded(() -> event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, TITLE,
                    "No files matched for " + TITLE + " (xml)", NotificationType.WARNING);
            return;
        }

        processFiles(indicator, toProcess);
    }

    private void processFiles(ProgressIndicator indicator, List<VirtualFile> files) {
        Environment env = getEnvironment();

        int skipped = 0;
        int missing = 0;
        AtomicInteger diffed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

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

            Map<String, MidPointObject> remoteObjects = new HashMap<>();

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

                    MidPointObject obj = MidPointObject.copy(object);
                    obj.setContent(newObject.getContent());
                    remoteObjects.put(obj.getOid(), obj);

                    diffed.incrementAndGet();
                } catch (Exception ex) {
                    failed.incrementAndGet();

                    midPointService.printToConsole(env, DiffRemoteTask.class, "Error getting object"
                            + object.getType().getTypeQName().getLocalPart() + "(" + object.getOid() + ")", ex);
                }
            }

            RunnableUtils.runWriteActionAndWait(() -> {

                Writer writer = null;
                VirtualFile vf = null;
                try {
                    List<String> deltas = new ArrayList<>();

                    for (MidPointObject local : objects) {
                        MidPointObject remote = remoteObjects.get(local.getOid());
                        if (remote == null) {
                            continue;
                        }

                        deltas.add(createDiffXml(local, file, LocationType.LOCAL, remote, null, LocationType.REMOTE));
                    }

                    vf = createScratchAndWriteDiff(deltas);
                } catch (Exception ex) {
                    failed.incrementAndGet();

                    midPointService.printToConsole(env, DiffRemoteTask.class, "Failed to compare file " + file.getPath(), ex);
                } finally {
                    IOUtils.closeQuietly(writer);
                }

                MidPointUtils.openFile(getProject(), vf);
            });
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
