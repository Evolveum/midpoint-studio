package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.ZipUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DownloadConnectorDevelopmentTask extends SimpleBackgroundableTask {

    private final Logger log = Logger.getInstance(this.getClass());

    public static String TITLE = "Download Connector Development task";

    public static String NOTIFICATION_KEY = TITLE;

    private static String ROOT_DIR_CONNID_CONNECTORS = "/connid-connectors";

    private final String bundleName;
    private final Consumer<File> onSuccess;

    public DownloadConnectorDevelopmentTask(
            @NotNull Project project,
            @NotNull Environment environment,
            @NotNull String bundleName,
            @NotNull Consumer<File> onSuccess
    ) {
        super(project, null, TITLE, NOTIFICATION_KEY);
        setEnvironment(environment);
        this.bundleName = bundleName;
        this.onSuccess = onSuccess;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        File tempJar = null;

        try {
            if (myProject == null) {
                throw new NullPointerException("Project is null");
            }

            indicator.setIndeterminate(true);
            indicator.setText("Download connector...");
            tempJar = client.downloadConnector(bundleName);

            if (tempJar == null) {
                throw new NullPointerException("Failed to download connector");
            }

            extractAndCreateSourceModule(myProject, tempJar.toPath(), bundleName);
            onSuccess.accept(tempJar);
        } finally {
            if (tempJar != null && tempJar.exists()) {
                try {
                    Files.delete(tempJar.toPath());
                    log.info("Temporary storage file erased cleanly.");
                } catch (IOException ioException) {
                    log.error(ioException);
                }
            }
        }
    }

    private void extractAndCreateSourceModule(@NotNull Project project, @NotNull Path tempJarPath, @NotNull String moduleName) {
        String basePath = project.getBasePath();
        if (basePath == null) return;

        try {
            File targetModuleDir = new File(basePath + ROOT_DIR_CONNID_CONNECTORS, moduleName);
            if (!targetModuleDir.exists()) {
                targetModuleDir.mkdirs();
            }

            ZipUtil.extract(tempJarPath.toFile(), targetModuleDir, null);

            ApplicationManager.getApplication().invokeAndWait(
                    () -> setupModuleFromExtractedSources(project, targetModuleDir, moduleName));

        } catch (IOException e) {
            log.error(e);
        }
    }

    private void setupModuleFromExtractedSources(@NotNull Project project, @NotNull File targetModuleDir, @NotNull String moduleName) {
        String imlPath = targetModuleDir.getAbsolutePath() + File.separator + moduleName + ".iml";

        WriteAction.run(() -> {
            Module newModule = ModuleManager.getInstance(project)
                    .newModule(imlPath, JavaModuleType.getModuleType().getId());

            VirtualFile contentRoot = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetModuleDir);
            if (contentRoot == null) return;

            ModuleRootManager rootManager = ModuleRootManager.getInstance(newModule);
            ModifiableRootModel rootModel = rootManager.getModifiableModel();

            ContentEntry contentEntry = rootModel.addContentEntry(contentRoot);

            VirtualFile srcFolder = contentRoot.findChild("src");

            if (srcFolder != null) {
                contentEntry.addSourceFolder(srcFolder, false);
            } else {
                contentEntry.addSourceFolder(contentRoot, false);
            }

            rootModel.inheritSdk();
            rootModel.commit();
        });
    }
}
