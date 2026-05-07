package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DiscoverDocumentationTask extends SimpleBackgroundableTask {

    public static String TITLE = "Discover documentation task";

    public static String NOTIFICATION_KEY = TITLE;

    private static final Logger LOG = Logger.getInstance(DiscoverDocumentationTask.class);

    private final String connectorDevelopmentOperationOid;

    public DiscoverDocumentationTask(@NotNull Project project,
                                     @NotNull Environment environment,
                                     @Nullable Supplier<DataContext> dataContextSupplier,
                                     @NotNull String connectorDevelopmentOperationOid
    ) {
        super(project, dataContextSupplier, TITLE, NOTIFICATION_KEY);
        this.connectorDevelopmentOperationOid = connectorDevelopmentOperationOid;
        setEnvironment(environment);
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        try {
            indicator.setText("Starting " + TITLE);
            var token = client.submitOperationDiscoverDocumentation(connectorDevelopmentOperationOid);

            ScheduledExecutorService executor =
                    AppExecutorUtil.getAppScheduledExecutorService();

            executor.scheduleWithFixedDelay(() -> {
                try {
                    var status = client.getStatusDiscoverDocumentation(token);
                    ApplicationManager.getApplication().invokeLater(() -> {

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Failed to start task: " + e.getMessage());
        }
    }
}
