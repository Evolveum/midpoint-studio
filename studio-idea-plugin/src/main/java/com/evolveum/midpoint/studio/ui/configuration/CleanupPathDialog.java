package com.evolveum.midpoint.studio.ui.configuration;

import com.evolveum.midpoint.studio.impl.configuration.CleanupPath;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CleanupPathDialog extends DialogWrapper {

    private final CleanupEditorPanel panel;

    public CleanupPathDialog(@Nullable Project project, @Nullable CleanupPath data) {
        super(project);

        panel = new CleanupEditorPanel(data);

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel.createComponent();
    }

    public CleanupPath getData() {
        return panel.getData();
    }
}
