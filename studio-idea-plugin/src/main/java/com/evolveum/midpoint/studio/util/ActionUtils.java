package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.task.DownloadTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ActionUtils {

    public static void runDownloadTask(@NotNull Project project, List<ObjectReferenceType> refs, boolean showOnly) {
        EnvironmentService es = EnvironmentService.getInstance(project);
        Environment env = es.getSelected();

        if (env == null) {
            MidPointUtils.publishNotification(
                    project, "Download missing", "Error", "No environment selected", NotificationType.WARNING);
            return;
        }

        List<Pair<String, ObjectTypes>> objectRefs = refs.stream()
                .map(ref -> new Pair<>(
                        ref.getOid(),
                        ObjectTypes.getObjectTypeFromTypeQName(ref.getType())))
                .toList();

        if (objectRefs.isEmpty()) {
            MidPointUtils.publishNotification(
                    project, "Download missing", "Information", "No references to download.", NotificationType.INFORMATION);
        }

        DownloadTask task = new DownloadTask(project, objectRefs, null, null, showOnly, true, false);
        task.setEnvironment(env);
        task.setOpenAfterDownload(false);

        ProgressManager.getInstance().run(task);
    }
}
