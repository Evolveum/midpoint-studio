package com.evolveum.midpoint.studio.ui.trace;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setStripeTitle("Trace");
        toolWindow.setTitle("Trace");

        ContentManager contentManager = toolWindow.getContentManager();

        Content variablesContent = buildVariables(project);
        contentManager.addContent(variablesContent);
        contentManager.setSelectedContent(variablesContent);

        Content logsContent = buildLogs(project);
        contentManager.addContent(logsContent);

//        Content trace
    }

    private Content buildVariables(Project project) {
        TraceVariablesPanel variables = new TraceVariablesPanel(project.getMessageBus());
        return ContentFactory.SERVICE.getInstance().createContent(variables, "Trace Tree", false);
    }

    private Content buildLogs(Project project) {
        TraceLogsPanel logs = new TraceLogsPanel(project.getMessageBus());
        return ContentFactory.SERVICE.getInstance().createContent(logs, "Logs", false);
    }

    @Override
    public void init(ToolWindow window) {
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        // todo improve with MidPointUtils.isMidPointFacetPresent(project);
        // also only if trace editor is opened
        return true;
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }
}