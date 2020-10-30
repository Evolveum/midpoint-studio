package com.evolveum.midpoint.studio.ui.performance;

import com.evolveum.midpoint.studio.ui.performance.singleOp.SingleOpPerformancePanel;
import com.evolveum.midpoint.studio.ui.trace.singleOp.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class PerformanceToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();

        Content overviewContent = buildPerformanceOverview(project);
        contentManager.addContent(overviewContent);
        contentManager.setSelectedContent(overviewContent);
    }

    private Content buildPerformanceOverview(Project project) {
        SingleOpPerformancePanel panel = new SingleOpPerformancePanel(project);
        return ContentFactory.SERVICE.getInstance().createContent(panel, "Performance Details", false);
    }

    @Override
    public void init(ToolWindow window) {
        window.setStripeTitle("Performance");
        window.setTitle("Performance");
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }
}
