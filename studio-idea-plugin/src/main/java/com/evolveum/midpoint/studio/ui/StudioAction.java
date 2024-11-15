package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Base class for UI actions for Studio.
 * By default, actions are enabled and visible only if MidPoint facet is present in the project.
 *
 * Action updated thread is {@link ActionUpdateThread#BGT}.
 */
public abstract class StudioAction extends AnAction {

    public StudioAction() {
    }

    public StudioAction(
            @Nullable @NlsActions.ActionText String text,
            @Nullable @NlsActions.ActionDescription String description,
            @Nullable Icon icon) {
        super(text, description, icon);
    }

    public StudioAction(@Nullable @NlsActions.ActionText String text) {
        super(text);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        boolean visible = isVisible(e);
        if (!visible) {
            e.getPresentation().setVisible(false);
            return;
        }

        boolean enabled = isEnabled(e);
        if (!enabled) {
            e.getPresentation().setEnabled(false);
        }
    }

    protected boolean isVisible(@NotNull AnActionEvent e) {
        return MidPointUtils.hasMidPointFacet(e.getProject());
    }

    protected boolean isEnabled(@NotNull AnActionEvent e) {
        return true;
    }
}
