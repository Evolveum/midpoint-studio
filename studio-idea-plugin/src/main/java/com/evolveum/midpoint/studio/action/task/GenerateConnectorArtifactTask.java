package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class GenerateConnectorArtifactTask extends SimpleBackgroundableTask {

    public static String TITLE = "Generate connector artifact task";

    public static String NOTIFICATION_KEY = TITLE;

    private static final Logger LOG = Logger.getInstance(GenerateConnectorArtifactTask.class);

    private final String connectorDevelopmentOperationOid;

    public GenerateConnectorArtifactTask(@NotNull Project project,
                                         @Nullable Supplier<DataContext> dataContextSupplier,
                                         @NotNull String connectorDevelopmentOperationOid
    ) {
        super(project, dataContextSupplier, TITLE, NOTIFICATION_KEY);

        EnvironmentService em = EnvironmentService.getInstance(project);
        Environment env = em.getSelected();
        setEnvironment(env);

        this.connectorDevelopmentOperationOid = connectorDevelopmentOperationOid;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        try {
            var result = client.createConnectorStatus(connectorDevelopmentOperationOid);
        } catch (Exception ex) {
            LOG.error("Couldn't generate connector artifact", ex);

            MidPointUtils.handleGenericException(getProject(), getEnvironment(), GenerateConnectorArtifactTask.class,
                    NOTIFICATION_KEY, "Couldn't generate connector artifact", ex);
        }
    }
}
