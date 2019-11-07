package com.evolveum.midpoint.studio.ui.trace;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowContentUiType;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceLensContextToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();

        TraceLensContextPanel lens = new TraceLensContextPanel();
        Content optionsContent = ContentFactory.SERVICE.getInstance().createContent(lens, null, false);
        contentManager.addContent(optionsContent);
    }

    @Override
    public void init(ToolWindow window) {
        window.setStripeTitle("Lens Context");
//        window.setTitle("Lens Context");
        window.setDefaultContentUiType(ToolWindowContentUiType.COMBO);
        window.setContentUiType(ToolWindowContentUiType.COMBO, null);
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