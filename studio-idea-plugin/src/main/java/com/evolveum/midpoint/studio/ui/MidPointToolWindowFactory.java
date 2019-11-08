package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.EnvironmentManager;
import com.evolveum.midpoint.studio.impl.MidPointManager;
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
    }

    private Content buildBrowser(Project project) {
        BrowseToolPanel browsePanel = new BrowseToolPanel();
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

        MidPointManager.getInstance(project).setConsole(consoleView);

        return ContentFactory.SERVICE.getInstance()
                .createContent(root, "Console", false);
    }

    private Content buildCredentials(Project project) {
        JPanel root = new JPanel(new BorderLayout());

        EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);

        CredentialsPanel credentialsPanel = new CredentialsPanel(project, environmentManager);
        root.add(credentialsPanel, BorderLayout.CENTER);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("CredentialsActions",
                new DefaultActionGroup(credentialsPanel.createConsoleActions()), false);
        root.add(toolbar.getComponent(), BorderLayout.WEST);

        return ContentFactory.SERVICE.getInstance().createContent(root, "Credentials", false);
    }

    @Override
    public void init(ToolWindow window) {
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true; // todo improve with MidPointUtils.isMidPointFacetPresent(project); also other actions, figure out how to listen to adding midpoitn facet to existing project
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }
}