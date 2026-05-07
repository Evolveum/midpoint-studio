package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDataModel;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConnectorProjectGenerator implements DirectoryProjectGenerator<Void> {

    private final String name;
    private final VirtualFile baseDir;

    public ConnectorProjectGenerator(ConnectorGeneratorDataModel connectorGeneratorDataModel) {
        this.name = connectorGeneratorDataModel.getProject().getName();
        this.baseDir = connectorGeneratorDataModel.getProject().getBaseDir();
    }

    @Override
    public @NotNull @NlsContexts.Label String getName() {
        return "";
    }

    @Override
    public @Nullable Icon getLogo() {
        return null;
    }

    @Override
    public void generateProject(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull Void unused, @NotNull Module module) {
        try {
            VirtualFile file = baseDir.createChildData(this, "README.md");
            file.setBinaryContent("# My Project".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull ValidationResult validate(@NotNull String s) {
        if (name.trim().isEmpty()) {
            return new ValidationResult("Project name cannot be empty");
        }

        if (!name.matches("[a-zA-Z0-9._-]+")) {
            return new ValidationResult("Invalid characters in project name");
        }

        return ValidationResult.OK;
    }
}
