package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class SynchronizationToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();

        SynchronizationPanel panel = new SynchronizationPanel(project);
        Content content = ContentFactory.getInstance().createContent(panel, null, false);
        contentManager.addContent(content);
    }

    @Override
    public void init(ToolWindow window) {
        window.setStripeTitle("Synchronization");
        window.setTitle("Synchronization");
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }
}
