package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.evolveum.midpoint.studio.impl.EnvironmentManager;
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

        EnvironmentManager manager = EnvironmentManager.getInstance(project);
        panel = new EnvironmentsPanel(manager.getFullSettings());

        setTitle("Edit Environments");

        setHorizontalStretch(2.5f);
        setVerticalStretch(1.5f);

        setOKActionEnabled(true);
        setOKButtonText("Save");
        setCancelButtonText("Cancel");

        setButtonsAlignment(SwingConstants.RIGHT);

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

        EnvironmentManager manager = EnvironmentManager.getInstance(project);
        manager.setSettings(panel.getFullSettings());
    }
}
