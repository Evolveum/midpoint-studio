package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.studio.action.task.SynchronizeObjectsTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class SynchronizationManager {

    private final Project project;

    private final Set<ToolWindow> toolWindows = new HashSet<>();

    // todo move this to some kind of SynchronizationSession object that will be simpler to manage in
    //  terms of threading (start/finish, cancel multiple ones)
    private final List<SyncFileItem> items = new ArrayList<>();

    private boolean running = false;

    public SynchronizationManager(@NotNull Project project) {
        this.project = project;
    }

    public static SynchronizationManager get(Project project) {
        return project.getService(SynchronizationManager.class);
    }

    public void attachToolWindow(@NotNull ToolWindow toolWindow) {
        // todo implement
    }

    public void synchronize(@NotNull List<VirtualFile> files, @NotNull Environment environment) {
        SynchronizeObjectsTask task = new SynchronizeObjectsTask(project, files);
        task.setEnvironment(environment);

        ProgressManager.getInstance().run(task);
    }

    public void add(@NotNull List<FileItem> items) {
        List<SyncFileItem> files = new ArrayList<>();

        for (FileItem fi : items) {
            List<SyncObjecItem> objects = new ArrayList<>();
            for (ObjectItem oi : fi.objects()) {
                objects.add(new SyncObjecItem(oi));
            }

            files.add(new SyncFileItem(fi, objects));
        }

        this.items.addAll(files);

        updateSynchronizationToolWindow(panel -> panel.getModel().addData(files));
    }

    // todo improve this
    public void start() {
        if (running == true) {
            return;
        }

        running = true;

        items.clear();

        updateSynchronizationToolWindow(panel -> panel.getModel().setData(new ArrayList<>()));
        // todo implement
    }

    private void updateSynchronizationToolWindow(Consumer<SynchronizationPanel> consumer) {
        RunnableUtils.invokeLaterIfNeeded(() -> {
            SynchronizationPanel panel = (SynchronizationPanel) ToolWindowManager.getInstance(project)
                    .getToolWindow("Synchronization").getContentManager().getContent(0).getComponent();
            consumer.accept(panel);
        });
    }

    public void finish() {
        running = false;

        RunnableUtils.invokeLaterIfNeeded(() -> ToolWindowManager.getInstance(project)
                .getToolWindow("Synchronization").show());

    }
}
