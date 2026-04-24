package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevDiscoverDocumentationResultType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DiscoverDocumentationTask extends SimpleBackgroundableTask {

    public static String TITLE = "Discover documentation task";

    public static String NOTIFICATION_KEY = TITLE;

    private static final Logger LOG = Logger.getInstance(DiscoverDocumentationTask.class);

    private final String connectorDevelopmentOperationOid;

    private ConnDevDiscoverDocumentationResultType connDevDiscoverDocumentationResultType;

    public DiscoverDocumentationTask(@NotNull Project project,
                                     @Nullable Supplier<DataContext> dataContextSupplier,
                                     @NotNull String connectorDevelopmentOperationOid
    ) {
        super(project, dataContextSupplier, TITLE, NOTIFICATION_KEY);
        this.connectorDevelopmentOperationOid = connectorDevelopmentOperationOid;

        EnvironmentService em = EnvironmentService.getInstance(project);
        Environment env = em.getSelected();
        setEnvironment(env);
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        try {
            var result = client.discoverDocumentationConnector(connectorDevelopmentOperationOid);
            ApplicationManager.getApplication().invokeLater(() -> {
                connDevDiscoverDocumentationResultType = result;
            });
        } catch (Exception ex) {
            LOG.error("Couldn't discover documentation", ex);

            MidPointUtils.handleGenericException(getProject(), getEnvironment(), DiscoverDocumentationTask.class,
                    NOTIFICATION_KEY, "Couldn't discover documentation", ex);
        }
    }

    public ConnDevDiscoverDocumentationResultType getConnDevDiscoverDocumentationResultType() {
        return connDevDiscoverDocumentationResultType;
    }
}
