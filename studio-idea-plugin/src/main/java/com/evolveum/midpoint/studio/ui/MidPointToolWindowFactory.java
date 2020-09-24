package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointService;
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
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setStripeTitle("MidPoint");
        toolWindow.setTitle("MidPoint");

        ContentManager contentManager = toolWindow.getContentManager();

        Content browseContent = buildBrowser(project);
        contentManager.addContent(browseContent);
        contentManager.setSelectedContent(browseContent);

        Content consoleContent = buildConsole(project);
        contentManager.addContent(consoleContent);

        Content credentialsContent = buildCredentials(project);
        contentManager.addContent(credentialsContent);

        Content credentialsContent2 = buildCredentials2(project);
        contentManager.addContent(credentialsContent2);
    }

    private Content buildBrowser(Project project) {
        BrowseToolPanel browsePanel = new BrowseToolPanel(project);
        return ContentFactory.SERVICE.getInstance()
                .createContent(browsePanel, "Browse Objects", false);
    }

    private Content buildConsole(Project project) {
        JPanel root = new JPanel(new BorderLayout());

        MidPointConsoleView consoleView = new MidPointConsoleView(project);
        Disposer.register(project, consoleView);

        root.add(consoleView.getComponent(), BorderLayout.CENTER);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("ConsoleActions",
                new DefaultActionGroup(consoleView.createConsoleActions()), false);
        root.add(toolbar.getComponent(), BorderLayout.WEST);

        MidPointService.getInstance(project).setConsole(consoleView);

        return ContentFactory.SERVICE.getInstance()
                .createContent(root, "Console", false);
    }

    private Content buildCredentials(Project project) {
        JPanel root = new JPanel(new BorderLayout());

        EnvironmentService environmentManager = EnvironmentService.getInstance(project);

        EnvironmentCredentials credentialsPanel = new EnvironmentCredentials(project, environmentManager);
        root.add(credentialsPanel, BorderLayout.CENTER);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("CredentialsActions",
                new DefaultActionGroup(credentialsPanel.createConsoleActions()), false);
        root.add(toolbar.getComponent(), BorderLayout.WEST);

        return ContentFactory.SERVICE.getInstance().createContent(root, "Credentials", false);
    }

    private Content buildCredentials2(Project project) {
        JPanel root = new JPanel(new BorderLayout());

        CredentialsPanel credentialsPanel = new CredentialsPanel(project);
        credentialsPanel.setBorder(JBUI.Borders.customLine(JBUI.CurrentTheme.DefaultTabs.borderColor(), 0, 1, 1, 1));
        root.add(credentialsPanel, BorderLayout.CENTER);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("CredentialsActions2",
                new DefaultActionGroup(credentialsPanel.createConsoleActions()), false);
        root.add(toolbar.getComponent(), BorderLayout.WEST);

        return ContentFactory.SERVICE.getInstance().createContent(root, "Credentials2", false);
    }

    @Override
    public void init(ToolWindow window) {
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }
}
