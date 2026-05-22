package com.evolveum.midpoint.studio.action.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.ZipUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnpackConnectorDevelopmentTask extends SimpleBackgroundableTask {

    public static String TITLE = "Open Connector Development task";

    public static String NOTIFICATION_KEY = TITLE;

    private final File connectorDevelopmentFile;

    public UnpackConnectorDevelopmentTask(
            @NotNull Project project,
            @NotNull File connectorDevelopmentFile
    ) {
        super(project, null, TITLE, NOTIFICATION_KEY);
        this.connectorDevelopmentFile = connectorDevelopmentFile;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        if (!connectorDevelopmentFile.exists()) {
            ApplicationManager.getApplication().invokeLater(() ->
                    Messages.showErrorDialog(getProject(), "Provided source ZIP file does not exist.", "Attachment Error")
            );
            return;
        }

        try {
            indicator.setIndeterminate(true);
            indicator.setText("Preparing workspace subdirectory...");

            String moduleFolderName = "connector-test";
            File targetModuleDir = new File(getProject().getBasePath(), moduleFolderName);

            if (targetModuleDir.exists()) {
                FileUtil.delete(targetModuleDir);
            }

            targetModuleDir.mkdirs();

            indicator.setText("Unpacking archive layers into project tree...");
            Path sourceZipPath = connectorDevelopmentFile.toPath();
            Path targetModulePath = targetModuleDir.toPath();
            ZipUtil.extract(sourceZipPath, targetModulePath, null);
            Files.deleteIfExists(sourceZipPath);

            ApplicationManager.getApplication().invokeLater(() -> {
                indicator.setText("Linking folder as a live project module...");

                WriteAction.run(() -> {
                    VirtualFile virtualModuleDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetModuleDir);
                    if (virtualModuleDir == null) return;
                    virtualModuleDir.refresh(false, true);
                    ModuleManager moduleManager = ModuleManager.getInstance(getProject());
                    String imlPath = targetModuleDir.getAbsolutePath() + File.separator + moduleFolderName + ".iml";

                    try {
                        Module newModule = moduleManager.newModule(imlPath, "JAVA_MODULE");
                        virtualModuleDir.refresh(true, true);
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to bind IDE submodule configuration alignment.", ex);
                    }
                });

                Messages.showInfoMessage(getProject(),
                        "Connector attached successfully as sub-project: " + moduleFolderName,
                        "Sub-module Attached");
            });
        } catch (IOException ex) {
            ApplicationManager.getApplication().invokeLater(() ->
                    Messages.showErrorDialog(getProject(), "Failed to unzip or map project structure: " + ex.getMessage(), "I/O Error")
            );
        }
    }
}
