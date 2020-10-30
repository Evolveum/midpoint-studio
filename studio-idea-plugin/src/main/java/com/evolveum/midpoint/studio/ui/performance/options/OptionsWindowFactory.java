package com.evolveum.midpoint.studio.ui.performance.options;

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
public class OptionsWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();

        OptionsPanel options = new OptionsPanel(project);
        Content optionsContent = ContentFactory.SERVICE.getInstance().createContent(options, null, false);
        contentManager.addContent(optionsContent);
    }

    @Override
    public void init(ToolWindow window) {
        window.setStripeTitle("Performance Analysis Options");
        window.setTitle("Performance Analysis Options");
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }
}
