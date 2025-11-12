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

        setTitle(title);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(250, 80));
        panel.add(label, BorderLayout.CENTER);
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
}