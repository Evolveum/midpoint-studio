package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.util.Holder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.progress.ProcessCanceledException;
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
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DownloadConnectorDevelopmentTask extends SimpleBackgroundableTask {

    private final Logger log = Logger.getInstance(DownloadConnectorDevelopmentTask.class);

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
                throw new NullPointerException("Project null");
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

        File targetModuleDir = new File(basePath + ROOT_DIR_CONNID_CONNECTORS, moduleName);
        File imlFile = new File(targetModuleDir, moduleName + ".iml");

        if (imlFile.exists() && !confirmModuleReplace(project, moduleName)) {
            throw new ProcessCanceledException();
        }

        try {
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

    private boolean confirmModuleReplace(@NotNull Project project, @NotNull String moduleName) {
        Holder<Integer> result = new Holder<>();

        ApplicationManager.getApplication().invokeAndWait(() ->
                result.setValue(Messages.showOkCancelDialog(
                        project,
                        "Module '%s' already exists. Do you want to replace it?".formatted(moduleName),
                        "Module Already Exists",
                        "OK",
                        "Cancel",
                        Messages.getWarningIcon()
                )));

        return result.getValue() != null && result.getValue() == Messages.OK;
    }

    private void setupModuleFromExtractedSources(@NotNull Project project, @NotNull File targetModuleDir, @NotNull String moduleName) {
        String imlPath = targetModuleDir.getAbsolutePath() + File.separator + moduleName + ".iml";

        WriteAction.run(() -> {
            ModuleManager moduleManager = ModuleManager.getInstance(project);
            Module module = moduleManager.findModuleByName(moduleName);
            if (module == null) {
                module = moduleManager.newModule(imlPath, JavaModuleType.getModuleType().getId());
            }

            VirtualFile contentRoot = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetModuleDir);
            if (contentRoot == null) return;

            ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();

            boolean alreadyRegistered = Arrays.stream(rootModel.getContentEntries())
                    .anyMatch(entry -> contentRoot.equals(entry.getFile()));

            if (alreadyRegistered) {
                rootModel.dispose();
                return;
            }

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
