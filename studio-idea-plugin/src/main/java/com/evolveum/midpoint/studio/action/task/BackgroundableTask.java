package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.Holder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    public void setEvent(AnActionEvent event) {
        this.event = event;
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

    protected List<MidPointObject> loadObjectsFromFile(VirtualFile file) throws Exception {
        return loadObjectsFromFile(file, false);
    }

    protected List<MidPointObject> loadObjectsFromFile(VirtualFile file, boolean includeObjectsOnly) throws Exception {
        List<MidPointObject> objects = new ArrayList<>();
        Holder<Exception> exception = new Holder<>();

        RunnableUtils.runWriteActionAndWait(() -> {
            MidPointUtils.forceSaveAndRefresh(getProject(), file);

            try {
                List<MidPointObject> obj = MidPointUtils.parseProjectFile(getProject(), file, notificationKey);
                obj = ClientUtils.filterObjectTypeOnly(obj, includeObjectsOnly);

                objects.addAll(obj);
            } catch (Exception ex) {
                exception.setValue(ex);
            }
        });

        if (!exception.isEmpty()) {
            throw exception.getValue();
        }

        return objects;
    }
}
