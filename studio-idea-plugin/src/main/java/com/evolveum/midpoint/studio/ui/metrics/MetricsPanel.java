package com.evolveum.midpoint.studio.ui.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MetricsPanel extends JPanel {

    private JPanel root;

    public MetricsPanel(@NotNull Project project, @NotNull VirtualFile file) {
        super(new BorderLayout());
        add(root, BorderLayout.CENTER);
    }
}
