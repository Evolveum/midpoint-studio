package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.studio.action.task.SynchronizeObjectsTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SynchronizationManager {

    private final Project project;

    // todo move this to some kind of SynchronizationSession object that will be simpler to manage in
    //  terms of threading (start/finish, cancel multiple ones)
    private final List<SynchronizationFileItem> items = new ArrayList<>();

    private boolean running = false;

    public SynchronizationManager(@NotNull Project project) {
        this.project = project;
    }

    public static SynchronizationManager get(Project project) {
        return project.getService(SynchronizationManager.class);
    }

    public void synchronize(@NotNull List<VirtualFile> files, @NotNull Environment environment) {
        SynchronizeObjectsTask task = new SynchronizeObjectsTask(project, files);
        task.setEnvironment(environment);

        ProgressManager.getInstance().run(task);
    }

    public void add(@NotNull List<SynchronizationFileItem> items) {
        items.addAll(items);

        // todo notify synchronization tree model in tool window

        RunnableUtils.invokeLaterIfNeeded(() -> {
            SynchronizationPanel panel = (SynchronizationPanel) ToolWindowManager.getInstance(project)
                    .getToolWindow("Synchronization").getContentManager().getContent(0).getComponent();
            panel.getModel().addData(items);
        });
    }

    public void start() {
        running = true;

        items.clear();

        RunnableUtils.invokeLaterIfNeeded(() -> {
            SynchronizationPanel panel = (SynchronizationPanel) ToolWindowManager.getInstance(project)
                    .getToolWindow("Synchronization").getContentManager().getContent(0).getComponent();
            panel.getModel().setData(new ArrayList<>());
        });

        // todo implement
    }

    public void finish() {
        running = false;
    }
}
