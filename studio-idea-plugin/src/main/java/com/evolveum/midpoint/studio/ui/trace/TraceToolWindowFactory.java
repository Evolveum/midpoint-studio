package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.MidPointManager;
import com.evolveum.midpoint.studio.ui.MidPointConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

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

        Content optionsContent = buildOptions(project);
        contentManager.addContent(optionsContent);
    }

    private Content buildVariables(Project project) {
        TraceVariablesPanel variables = new TraceVariablesPanel();
        return ContentFactory.SERVICE.getInstance().createContent(variables, "Variables", false);
    }

    private Content buildLogs(Project project) {
        TraceLogsPanel logs = new TraceLogsPanel();
        return ContentFactory.SERVICE.getInstance().createContent(logs, "Logs", false);
    }

    private Content buildOptions(Project project) {
        TraceOptionsPanel options = new TraceOptionsPanel();
        return ContentFactory.SERVICE.getInstance().createContent(options, "Options", false);
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