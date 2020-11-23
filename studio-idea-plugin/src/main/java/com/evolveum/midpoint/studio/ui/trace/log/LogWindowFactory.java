package com.evolveum.midpoint.studio.ui.trace.log;

import com.evolveum.midpoint.studio.ui.trace.TraceUtils;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LogWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();

        TraceLogsPanel logs = new TraceLogsPanel(project.getMessageBus());
        Content logsContent = ContentFactory.SERVICE.getInstance().createContent(logs, "Logs", false);
        contentManager.addContent(logsContent);
    }

    @Override
    public void init(ToolWindow window) {
        window.setStripeTitle("Log");
        window.setTitle("Log");
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return TraceUtils.shouldBeVisible(project);
    }
}
