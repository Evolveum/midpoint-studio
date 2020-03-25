package com.evolveum.midpoint.studio.ui.trace;

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

        Content traceEntryDetails = buildTraceEntryDetails(project);
        contentManager.addContent(traceEntryDetails);

        Content traceEntryDetailsRaw = buildTraceEntryDetailsRaw(project);
        contentManager.addContent(traceEntryDetailsRaw);
    }

    private Content buildTraceEntryDetails(Project project) {
        TraceEntryDetailsPanel panel = new TraceEntryDetailsPanel(project);
        return ContentFactory.SERVICE.getInstance().createContent(panel, "Trace Entry Details", false);
    }

    private Content buildTraceEntryDetailsRaw(Project project) {
        TraceEntryDetailsRawPanel panel = new TraceEntryDetailsRawPanel(project);
        return ContentFactory.SERVICE.getInstance().createContent(panel, "Trace Entry Details Raw", false);
    }

    private Content buildVariables(Project project) {
        TraceTreePanel variables = new TraceTreePanel(project.getMessageBus());
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