package com.evolveum.midpoint.studio.impl.ide.error;

import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class JiraFeedbackTask extends Backgroundable {

    private final Consumer<SubmittedReportInfo> callback;

    private ReporterError error;

    private String username;

    private String password;

    JiraFeedbackTask(@Nullable Project project,
                     @NotNull String title,
                     boolean canBeCancelled,
                     ReporterError error,
                     String username,
                     String password,
                     Consumer<SubmittedReportInfo> callback) {

        super(project, title, canBeCancelled);

        this.error = error;
        this.username = username;
        this.password = password;

        this.callback = callback;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true);

        JiraReporter reporter = new JiraReporter(username, password);
        callback.consume(reporter.sendFeedback(error));
    }
}
