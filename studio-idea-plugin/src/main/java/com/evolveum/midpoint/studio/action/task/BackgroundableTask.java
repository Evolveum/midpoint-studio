package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class BackgroundableTask extends Task.Backgroundable {

    private static final Logger LOG = Logger.getInstance(ObjectsBackgroundableTask.class);

    protected MidPointService midPointService;

    protected AnActionEvent event;

    protected String notificationKey;

    private Environment environment;

    public BackgroundableTask(@NotNull Project project, @NotNull String title, @NotNull String notificationKey) {
        super(project, title, true);

        this.midPointService = MidPointService.getInstance(project);

        this.notificationKey = notificationKey;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        LOG.info("Starting task: " + getClass().getName());

        if (getProject() == null) {
            LOG.debug("Project object is null, stopping task");
            return;
        }

        new RunnableUtils.PluginClasspathRunnable() {

            @Override
            public void runWithPluginClassLoader() {
                doRun(indicator);
            }
        }.run();

        LOG.info("Task finished: " + getClass().getName());
    }

    protected abstract void doRun(ProgressIndicator indicator);
}
