package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SimpleCheckboxAction extends CheckboxAction {

    private boolean selected;

    public SimpleCheckboxAction(String text) {
        super(text);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return selected;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        selected = state;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    @NotNull
    @Override
    public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        JBCheckBox check = (JBCheckBox) super.createCustomComponent(presentation, place);
        check.addChangeListener(e -> stateChanged(e));

        return check;
    }

    public void stateChanged(ChangeEvent e) {

    }
}
