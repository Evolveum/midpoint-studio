package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SynchronizeObjectsTask extends SynchronizationTask {

    private static final Logger LOG = Logger.getInstance(SynchronizeObjectsTask.class);

    public static String TITLE = "Synchronize objects";

    public static String NOTIFICATION_KEY = TITLE;

    private final List<VirtualFile> files;

    public SynchronizeObjectsTask(
            @NotNull Project project, @NotNull SynchronizationSession<?> session, @NotNull List<VirtualFile> files) {
        super(project, TITLE, NOTIFICATION_KEY, session);

        this.files = files;
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

        Counter counter = new Counter();

        getSession().open();

        int current = 0;
        for (VirtualFile file : files) {
            ProgressManager.checkCanceled();

            current++;
            indicator.setFraction((double) current / files.size());

            SynchronizationFileItem<SynchronizationObjectItem> fileItem = createSynchronizationFileItem(counter, file);
            if (fileItem == null) {
                continue;
            }

            getSession().addFileItem((SynchronizationFileItem) fileItem);
        }


        SynchronizationManager.get(getProject()).showSynchronizationToolWindow(true);

        NotificationType type = counter.missing > 0 || counter.failed > 0 || counter.skipped > 0 ?
                NotificationType.WARNING : NotificationType.INFORMATION;

        StringBuilder msg = new StringBuilder();
        msg.append("Compared ").append(counter.processed).append(" objects<br/>");
        msg.append("Missing ").append(counter.missing).append(" objects<br/>");
        msg.append("Failed to compare ").append(counter.failed).append(" objects<br/>");
        msg.append("Skipped ").append(counter.skipped).append(" files");
        MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, TITLE, msg.toString(), type);
    }
}
