package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnvironmentListDialog extends DialogWrapper {

    private Project project;

    private EnvironmentsPanel panel;

    public EnvironmentListDialog(Project project) {
        super(project, true);

        this.project = project;

        MidPointService ms = MidPointService.getInstance(project);
        EnvironmentService manager = EnvironmentService.getInstance(project);

        panel = new EnvironmentsPanel(project, ms.getSettings(), manager.getFullSettings());

        setTitle("Edit Environments");

        setHorizontalStretch(2.5f);
        setVerticalStretch(1.5f);

        setOKActionEnabled(true);
        setOKButtonText("Save");
        setCancelButtonText("Cancel");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(panel);

        return root;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();

        EnvironmentService manager = EnvironmentService.getInstance(project);
        manager.setSettings(panel.getFullSettings());
    }
}
