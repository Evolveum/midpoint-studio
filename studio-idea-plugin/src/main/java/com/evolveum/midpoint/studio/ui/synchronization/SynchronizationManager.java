package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SynchronizationManager {

    private final Project project;

    private final SynchronizationPanel panel;

    private SynchronizationSession<?> session;

    public SynchronizationManager(@NotNull Project project) {
        this.project = project;

        panel = new SynchronizationPanel(project);
    }

    public static SynchronizationManager get(Project project) {
        return project.getService(SynchronizationManager.class);
    }

    public void attachToolWindow(@NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();

        Content content = ContentFactory.getInstance().createContent(panel, null, false);
        contentManager.addContent(content);
    }

    private synchronized SynchronizationSession<?> createSession(Environment environment) {
        if (session != null) {
            session.close();
        }

        session = new SynchronizationSession<>(project, environment, panel);

        RunnableUtils.invokeLaterIfNeeded(() -> {
            panel.getModel().setData(new ArrayList<>());
        });

        return session;
    }

    public synchronized SynchronizationSession<?> getSession() {
        return session;
    }

    public synchronized void synchronize(
            @NotNull List<VirtualFile> files,
            @NotNull Environment environment) {

        SynchronizationSession<?> session = createSession(environment);

        SynchronizeObjectsTask task = new SynchronizeObjectsTask(project, session, files);
        task.setEnvironment(environment);

        ProgressManager.getInstance().run(task);
    }

    private ToolWindow getSynchronizationToolWindow() {
        return ToolWindowManager.getInstance(project).getToolWindow("Synchronization");
    }

    public void showSynchronizationToolWindow(boolean expand) {
        RunnableUtils.invokeLaterIfNeeded(() -> {
            getSynchronizationToolWindow().show();

            if (expand) {
                panel.expandTree();
            }
        });
    }
}
