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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // intentionally left empty
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
        return new JLabel(presentation.getText());
    }
}
