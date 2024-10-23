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
public abstract class SimpleBackgroundableTask extends BackgroundableTask {

    private static final Logger LOG = Logger.getInstance(SimpleBackgroundableTask.class);

    protected MidPointClient client;

    public SimpleBackgroundableTask(
            @NotNull Project project, @Nullable Supplier<DataContext> dataContextSupplier, @NotNull String title,
            @NotNull String notificationKey) {
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
    }

    private MidPointClient setupMidpointClient() {
        logToConsole("Setting up MidPoint client");

        Environment env = getEnvironment();

        midPointService.printToConsole(env, getClass(), "Initializing '" + getTitle() + "' action");

        MidPointClient client = new MidPointClient(getProject(), env);

        logToConsole("MidPoint client setup done");

        return client;
    }
}
