package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.ObjectReferencesConfiguration;
import com.evolveum.midpoint.studio.ui.MissingObjectRefsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogPanel;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class MissingObjectRefsDialog extends DialogWrapper {

    private final MissingObjectRefsEditor editor;

    private final DialogPanel panel;

    public MissingObjectRefsDialog(@Nullable Project project, @NotNull List<ObjectReferencesConfiguration> objects) {
        super(project);

        editor = new MissingObjectRefsEditor(project, objects);
        panel = editor.createComponent();

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    public @NotNull List<ObjectReferencesConfiguration> getData() {
        return List.of(); // todo implement
    }
}
