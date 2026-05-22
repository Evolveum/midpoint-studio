package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.function.Consumer;

public class DownloadConnectorDevelopmentTask extends SimpleBackgroundableTask {

    public static String TITLE = "Download Connector Development task";

    public static String NOTIFICATION_KEY = TITLE;

    private final String bundleName;
    private final String version;
    private final Consumer<File> onSuccess;

    public DownloadConnectorDevelopmentTask(
            @NotNull Project project,
            @NotNull Environment environment,
            @NotNull String bundleName,
            @NotNull String version,
            @NotNull Consumer<File> onSuccess
    ) {
        super(project, null, TITLE, NOTIFICATION_KEY);
        setEnvironment(environment);
        this.bundleName = bundleName;
        this.version = version;
        this.onSuccess = onSuccess;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        try {
            indicator.setIndeterminate(true);
            indicator.setText("Downloading connector...");
            File tempZip = client.downloadConnector(bundleName, version);
            onSuccess.accept(tempZip);
        } catch (Exception ex) {
            ApplicationManager.getApplication().invokeLater(() ->
                    Messages.showErrorDialog(getProject(), "Download pipeline failed: " + ex.getMessage(), "API Task Error")
            );
        }
    }
}

