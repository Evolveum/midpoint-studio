package com.evolveum.midpoint.studio.ui.trace.graph;

import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class TraceGraphWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();

        TraceGraphPanel graphPanel = new TraceGraphPanel(project);
        Content graphContent = ContentFactory.getInstance().createContent(graphPanel, null, false);
        contentManager.addContent(graphContent);
    }

    @Override
    public void init(ToolWindow window) {
        window.setStripeTitle("Trace Graph");
        window.setTitle("Trace Graph");
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return TraceUtils.shouldBeVisible(project);
    }

}
