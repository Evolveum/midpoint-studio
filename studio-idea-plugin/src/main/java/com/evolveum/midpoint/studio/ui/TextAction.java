package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TextAction extends AnAction implements CustomComponentAction {

    private JLabel label;

    public TextAction() {
        label = new JLabel("presentation.getText()");
        label.setMinimumSize(new Dimension(30, 30));
        label.setMaximumSize(new Dimension(500, 30));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // intentionally left empty
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String text = createText(e);

        e.getPresentation().setText(text);
        if (label != null) {
            label.setText(text);
        }
    }

    protected String createText(AnActionEvent evt) {
        return "";
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
        return label;
    }
}
