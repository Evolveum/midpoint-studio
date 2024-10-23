package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ClientBackgroundableTask<S extends TaskState> extends ObjectsBackgroundableTask<S> {

    private static final Logger LOG = Logger.getInstance(ClientBackgroundableTask.class);

    protected MidPointClient client;

    public ClientBackgroundableTask(@NotNull Project project, @Nullable Supplier<DataContext> dataContextSupplier,
                                    @NotNull String title, @NotNull String notificationKey) {
        super(project, dataContextSupplier, title, notificationKey);
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        if (getProject() == null) {
            return;
        }

        if (getEnvironment() == null) {
            return;
        }

        client = setupMidpointClient();

        super.doRun(indicator);
    }

    protected MidPointClient setupMidpointClient() {
        LOG.debug("Setting up MidPoint client");

        Environment env = getEnvironment();

        midPointService.printToConsole(env, getClass(), "Setting up midPoint client");

        MidPointClient client = new MidPointClient(getProject(), env);

        LOG.debug("MidPoint client setup done");

        return client;
    }
}
