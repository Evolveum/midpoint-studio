package com.evolveum.midpoint.studio.ui.configuration;

import com.evolveum.midpoint.studio.impl.configuration.CleanupPathConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogPanel;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CleanupPathDialog extends DialogWrapper {

    private final CleanupPathConfigurationEditor editor;

    private final DialogPanel panel;

    public CleanupPathDialog(@NotNull Project project, @Nullable CleanupPathConfiguration data) {
        super(project);

        editor = new CleanupPathConfigurationEditor(project, data);
        panel = editor.createComponent();

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    public CleanupPathConfiguration getData() {
        return editor.getData();
    }
}
