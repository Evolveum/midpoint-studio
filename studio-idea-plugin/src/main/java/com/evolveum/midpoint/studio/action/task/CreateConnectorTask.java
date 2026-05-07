package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnDevCreateConnectorResultType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CreateConnectorTask extends SimpleBackgroundableTask {

    public static String TITLE = "Create connector task";

    public static String NOTIFICATION_KEY = TITLE;

    private static final Logger LOG = Logger.getInstance(CreateConnectorTask.class);

    private final String connectorDevelopmentOperationOid;

    private ConnDevCreateConnectorResultType connDevCreateConnectorResultType;

    public CreateConnectorTask(@NotNull Project project,
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
//            var result = client.createConnectorStatus(connectorDevelopmentOperationOid);
//            ApplicationManager.getApplication().invokeLater(() -> {
//                connDevCreateConnectorResultType = result;
//            });
        } catch (Exception ex) {
            LOG.error("Couldn't discover documentation", ex);

            MidPointUtils.handleGenericException(getProject(), getEnvironment(), DiscoverDocumentationTask.class,
                    NOTIFICATION_KEY, "Couldn't discover documentation", ex);
        }
    }

    public ConnDevCreateConnectorResultType getConnDevCreateConnectorResultType() {
        return connDevCreateConnectorResultType;
    }
}
