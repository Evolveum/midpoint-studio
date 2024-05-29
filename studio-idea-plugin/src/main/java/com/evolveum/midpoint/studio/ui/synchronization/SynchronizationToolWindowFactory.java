package com.evolveum.midpoint.studio.ui.synchronization;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class SynchronizationToolWindowFactory implements ToolWindowFactory, DumbAware {

    private static final String TITLE = "Objects synchronization";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SynchronizationManager.get(project).attachToolWindow(toolWindow);
    }

    @Override
    public void init(ToolWindow window) {
        window.setStripeTitle(TITLE);
        window.setTitle(TITLE);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }
}
