package com.evolveum.midpoint.studio.ui.trace;

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
 * Created by Viliam Repan (lazyman).
 */
public class TraceToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();

        Content overviewContent = buildTraceOverview(project);
        contentManager.addContent(overviewContent);
        contentManager.setSelectedContent(overviewContent);

        Content variablesContent = buildTraceTree(project);
        contentManager.addContent(variablesContent);

        Content tracePerformanceInformation = buildTracePerformance(project);
        contentManager.addContent(tracePerformanceInformation);

        Content traceEntryDetails = buildTraceEntryDetails(project);
        contentManager.addContent(traceEntryDetails);

        Content traceEntryDetailsRaw = buildTraceEntryDetailsRaw(project);
        contentManager.addContent(traceEntryDetailsRaw);

        Content operationResultRaw = buildOperationResultRaw(project);
        contentManager.addContent(operationResultRaw);
    }

    private Content buildTraceEntryDetails(Project project) {
        OpDumpPanel panel = new OpDumpPanel(project);
        return ContentFactory.SERVICE.getInstance().createContent(panel, "Operation Details", false);
    }

    private Content buildTraceEntryDetailsRaw(Project project) {
        OpTraceRawPanel panel = new OpTraceRawPanel(project);
        return ContentFactory.SERVICE.getInstance().createContent(panel, "Trace Entries Raw", false);
    }

    private Content buildOperationResultRaw(Project project) {
        OpResultRawPanel panel = new OpResultRawPanel(project);
        return ContentFactory.SERVICE.getInstance().createContent(panel, "Operation Raw", false);
    }

    private Content buildTraceTree(Project project) {
        OpDetailsTreePanel variables = new OpDetailsTreePanel(project);
        return ContentFactory.SERVICE.getInstance().createContent(variables, "Tree View", false);
    }

    private Content buildTraceOverview(Project project) {
        OpOverviewTreePanel variables = new OpOverviewTreePanel(project);
        return ContentFactory.SERVICE.getInstance().createContent(variables, "Overview", false);
    }

    private Content buildTracePerformance(Project project) {
        OpPerformancePanel perfInformation = new OpPerformancePanel(project.getMessageBus());
        //return new HeaderDecorator("Trace Performance Information", new JBScrollPane(perfInformation));
        return ContentFactory.SERVICE.getInstance().createContent(perfInformation, "Performance Information", false);
    }

    @Override
    public void init(ToolWindow window) {
        window.setStripeTitle("Trace");
        window.setTitle("Trace");
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }
}
