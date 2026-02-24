/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.dialog.alert;

import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DialogAlert extends DialogWrapper {
    private final DialogWindowActionHandler handler;
    private final String message;

    public DialogAlert(@Nullable Project project, String title, String message, DialogWindowActionHandler handler) {
        super(project);
        this.message = message;
        this.handler = handler;

        init();
        setTitle(title);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea textArea = new JTextArea(message);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFocusable(false);

        textArea.setFont(UIManager.getFont("Label.font"));
        textArea.setForeground(UIManager.getColor("Label.foreground"));

        panel.add(textArea, BorderLayout.CENTER);

        return panel;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        if (handler != null) {
            handler.onOk();
        }
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
        if (handler != null) {
            handler.onCancel();
        }
    }

    @Override
    public @Nullable Dimension getInitialSize() {
        return new Dimension(600, 80);
    }
}