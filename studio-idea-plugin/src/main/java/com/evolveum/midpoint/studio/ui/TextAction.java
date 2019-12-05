package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TextAction extends AnAction implements CustomComponentAction {

    private JLabel label;
    private String text;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // intentionally left empty
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        e.getPresentation().setText(text);
        if (label != null) {
            label.setText(text);
        }
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    @NotNull
    @Override
    public JComponent createCustomComponent(@NotNull Presentation presentation) {
        return createCustomComponent(presentation, ActionPlaces.UNKNOWN);
    }

    @NotNull
    @Override
    public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        label = new JLabel(presentation.getText());
        return label;
    }
}
