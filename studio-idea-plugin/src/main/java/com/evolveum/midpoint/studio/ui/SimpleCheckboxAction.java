package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SimpleCheckboxAction extends CheckboxAction {

    private boolean selected;

    public SimpleCheckboxAction(String text) {
        super(text);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return selected;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        if (selected != state) {
            selected = state;
            onStateChange();
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void onStateChange() {
    }
}
