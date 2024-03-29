package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.configuration.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DownloadMissingNotificationAction extends NotificationAction {

    private static final String TEXT = "Download missing";

    private final Project project;

    private final List<MissingRefObject> data;

    public DownloadMissingNotificationAction(
            @NotNull Project project, @NotNull List<MissingRefObject> data) {
        super(TEXT);

        this.project = project;
        this.data = data;


        // filter ignored missing references based on project configuration (cleanup/missing settings)
//        List<ObjectReferenceType> downloadOnly = computeDownloadOnly(object.getOid(), object.getType(), missingReferences);
//
//        List<ObjectReferenceType> downloadOnly = List.of(); // todo implement
//        if (!downloadOnly.isEmpty()) {
//            actions.add(new DownloadMissingNotificationAction(downloadOnly));
//        }
    }

    @Override
    public void actionPerformed(AnActionEvent e, Notification notification) {
        Project project = e.getProject();

// todo implement
        //        ActionUtils.runDownloadTask(project, references, false);
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    public boolean isVisible() {
        return true;// todo implement
    }

    private List<ObjectReferenceType> computeDownloadOnly(
            String oid, ObjectTypes type, List<ObjectReferenceType> missingReferences) {

        MissingRefObjects missingRefsConfig = CleanupService.get(project).getSettings().getMissingReferences();
        MissingRefObject objectRefsConfig = missingRefsConfig.getObjects().stream()
                .filter(orc -> Objects.equals(oid, orc.getOid()))
                .findFirst()
                .orElse(null);

        Map<String, MissingRefAction> map = objectRefsConfig.getReferences().stream()
                .collect(Collectors.toMap(MissingRef::getOid, o -> o.getAction()));

        // todo use this to filter existing files
//        ObjectFileBasedIndexImpl.getVirtualFiles()

        return missingReferences.stream()
                .filter(o -> {
                    if (objectRefsConfig == null
                            || Objects.equals(missingRefsConfig.getDefaultAction(), MissingRefAction.ALWAYS_DOWNLOAD)) {
                        return true;
                    }

//                    map.get
//                    return objectRefsConfig.getReferences().stream()
//                            .noneMatch(orc -> Objects.equals(orc.getOid(), o.getOid()));

                    // todo implement
                    return true;
                })
                .toList();
    }
}
