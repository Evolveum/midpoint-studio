package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.EnvironmentService;
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

    public static final String WINDOW_ID = "MidPoint";

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

        Content propertiesContent = buildEncryptedProperties(project);
        contentManager.addContent(propertiesContent);
    }

    private Content buildBrowser(Project project) {
        BrowseToolPanel browsePanel = new BrowseToolPanel(project);
        return ContentFactory.SERVICE.getInstance()
                .createContent(browsePanel, "Browse Objects", false);
    }

    private Content buildConsole(Project project) {
        MidPointConsoleView consoleView = new MidPointConsoleView(project);
        Disposer.register(project, consoleView);

        MidPointConsolePanel root = new MidPointConsolePanel(consoleView);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("ConsoleActions",
                new DefaultActionGroup(consoleView.createConsoleActions()), false);
        toolbar.setTargetComponent(root);

        root.setToolbar(toolbar);

        return ContentFactory.SERVICE.getInstance()
                .createContent(root, "Console", false);
    }

    private Content buildEncryptedProperties(Project project) {
        JPanel root = new JPanel(new BorderLayout());

        EnvironmentService environmentManager = EnvironmentService.getInstance(project);

        EncryptedPropertiesPanel propertiesPanel = new EncryptedPropertiesPanel(project, environmentManager);
        root.add(propertiesPanel, BorderLayout.CENTER);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("EncryptedPropertiesActions",
                new DefaultActionGroup(propertiesPanel.createConsoleActions()), false);
        toolbar.setTargetComponent(root);
        root.add(toolbar.getComponent(), BorderLayout.WEST);

        return ContentFactory.SERVICE.getInstance().createContent(root, "Encrypted Properties", false);
    }

    @Override
    public void init(ToolWindow window) {
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }
}
