package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.xml.LocationType;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DiffLocalTask extends DiffTask {

    private static final Logger LOG = Logger.getInstance(DiffLocalTask.class);

    public static String TITLE = "Diff local task";

    public static String NOTIFICATION_KEY = TITLE;

    public DiffLocalTask(@NotNull AnActionEvent event) {
        super(event, TITLE, NOTIFICATION_KEY);
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);
        if (toProcess.size() != 2) {
            midPointService.printToConsole(getEnvironment(), DiffLocalTask.class, "Too many files selected, should be only two.");
            return;
        }

        VirtualFile firstFile = toProcess.get(0);
        VirtualFile secondFile = toProcess.get(1);

        List<MidPointObject> firstSet = loadObjects(firstFile);
        if (firstSet == null) {
            return;
        }

        List<MidPointObject> secondSet = loadObjects(secondFile);
        if (secondSet == null) {
            return;
        }

        if (firstSet.size() != secondSet.size()) {
            midPointService.printToConsole(getEnvironment(), DiffLocalTask.class,
                    "Number of objects doesn't match in selected files: " + firstSet.size() + " vs. " + secondSet.size());
            return;
        }

        RunnableUtils.runWriteActionAndWait(() -> {

            Writer writer = null;
            VirtualFile vf = null;
            try {
                List<String> deltas = new ArrayList<>();

                for (int i = 0; i < firstSet.size(); i++) {
                    MidPointObject first = firstSet.get(i);
                    MidPointObject second = secondSet.get(i);

                    deltas.add(createDiffXml(first, firstFile, LocationType.LOCAL, second, secondFile, LocationType.LOCAL));
                }

                vf = createScratchAndWriteDiff(deltas);
            } catch (Exception ex) {
//                failed.incrementAndGet();
//
                midPointService.printToConsole(getEnvironment(), DiffLocalTask.class, "Failed to compare files "
                        + firstFile.getPath() + " with " + secondFile.getPath(), ex);
            } finally {
                IOUtils.closeQuietly(writer);
            }

            MidPointUtils.openFile(getProject(), vf);
        });
    }

    private List<MidPointObject> loadObjects(VirtualFile file) {
        List<MidPointObject> objects = null;
        try {
            objects = loadObjectsFromFile(file);
        } catch (Exception ex) {
            // todo increment failed
            midPointService.printToConsole(getEnvironment(), DiffLocalTask.class, "Couldn't load objects from file " + file.getPath(), ex);
        }

        return objects;
    }
}
