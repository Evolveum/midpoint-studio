package com.evolveum.midpoint.studio.impl.ide.error;

import com.intellij.diagnostic.ReportMessages;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CallbackWithNotification implements Consumer<SubmittedReportInfo> {

    private Consumer<SubmittedReportInfo> originalConsumer;

    private Project project;

    public CallbackWithNotification(Consumer<SubmittedReportInfo> originalConsumer, Project project) {
        this.originalConsumer = originalConsumer;
        this.project = project;
    }

    @Override
    public void consume(SubmittedReportInfo reportInfo) {
        originalConsumer.consume(reportInfo);

        if (reportInfo.getStatus().equals(SubmittedReportInfo.SubmissionStatus.FAILED)) {
            ReportMessages.GROUP.createNotification(
                    ReportMessages.ERROR_REPORT,
                    reportInfo.getLinkText(),
                    NotificationType.ERROR,
                    null).setImportant(false).notify(project);
        } else {
            ReportMessages.GROUP.createNotification(
                    ReportMessages.ERROR_REPORT,
                    reportInfo.getLinkText(),
                    NotificationType.INFORMATION,
                    NotificationListener.URL_OPENING_LISTENER).setImportant(false).notify(project);
        }

    }
}
