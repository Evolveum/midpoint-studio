package com.evolveum.midpoint.studio.impl.ide;

import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.MidPointIcons;
import com.evolveum.midpoint.studio.impl.EncryptionService;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.impl.ProjectSettings;
import com.evolveum.midpoint.studio.ui.MidPointWizardStep;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.StudioBundle;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenUtil;
import org.jetbrains.idea.maven.wizards.AbstractMavenModuleBuilder;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointModuleBuilder extends AbstractMavenModuleBuilder {

    private static final Logger LOG = Logger.getInstance(MidPointModuleBuilder.class);

    public static final String NOTIFICATION_KEY = "Module";

    public static String MODULE_NAME = "MidPoint";

    private final ProjectSettings settings = new ProjectSettings();

    public MidPointModuleBuilder() {
        setName(MODULE_NAME);
    }

    @Override
    public String getPresentableName() {
        return MODULE_NAME;
    }

    @Override
    public String getGroupName() {
        return MODULE_NAME;
    }

    @Override
    public String getDescription() {
        return "MidPoint module based on Maven build system.";
    }

    @Override
    public Icon getNodeIcon() {
        return MidPointIcons.Midpoint;
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[0];
    }

    @Override
    public void setupRootModel(ModifiableRootModel root) {
        MavenUtil.runWhenInitialized(root.getProject(), (DumbAwareRunnable) () -> setupProject(root));
    }

    private void setupProject(ModifiableRootModel root) {
        Project project = root.getProject();

        String contentPath = getContentEntryPath();
        String path = FileUtil.toSystemIndependentName(contentPath);

        try {
            Files.createDirectories(Path.of(path));
        } catch (IOException ex) {
            LOG.error("Couldn't create content path", ex);
        }

        VirtualFile rootFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);

        rootFile.refresh(true, false, () -> {
            createProjectFiles(project, rootFile);

            MavenProjectsManager mpm = MavenProjectsManager.getInstance(project);
            mpm.forceUpdateAllProjectsOrFindAllAvailablePomFiles();

            MavenManagerListener listener = new MavenManagerListener(project) {

                private boolean executedOnce;

                @Override
                public void projectImportCompleted() {
                    if (executedOnce) {
                        return;
                    }

                    super.projectImportCompleted();

                    executedOnce = true;
                }
            };
            mpm.addManagerListener(listener);
        });
    }

    public void createProjectFiles(Project project, VirtualFile rootFile) {
        try {
            // create pom.xml
            WriteCommandAction.writeCommandAction(project)
                    .withName(StudioBundle.message("midpoint.new.project")).compute(() -> {
                        Properties properties = new Properties();

                        String escaped = rootFile.getName().replaceAll("[^a-zA-Z0-9_-]", "");

                        properties.setProperty("GROUP_ID", escaped);
                        properties.setProperty("ARTIFACT_ID", escaped);
                        properties.setProperty("VERSION", "1.0-SNAPSHOT");

                        properties.setProperty("PROJECT_NAME", escaped);

                        properties.setProperty("MIDPOINT_VERSION", MidPointConstants.DEFAULT_MIDPOINT_VERSION);

                        return createPomFile(project, rootFile, properties);
                    });

            VfsUtil.createDirectories(rootFile.getPath() + "/objects");
            VfsUtil.createDirectories(rootFile.getPath() + "/scratches");

            createGitIgnoreFile(project, rootFile);
        } catch (IOException ex) {
            MidPointUtils.publishExceptionNotification(project, null, MidPointModuleBuilder.class, NOTIFICATION_KEY, "Couldn't create project files", ex);
        }
    }

    private void createGitIgnoreFile(Project project, VirtualFile root) throws IOException {
        final String GITIGNORE = ".gitignore";

        VirtualFile file = root.findChild(GITIGNORE);
        if (file != null && file.exists()) {
            LOG.info("File '" + GITIGNORE + "' already exits");
            return;
        }

        file = root.createChildData(this, GITIGNORE);

        FileTemplateManager ftManager = FileTemplateManager.getInstance(project);
        FileTemplate fileTemplate = ftManager.getTemplate(MidPointFileTemplateGroupFactory.MIDPOINT_GIT_IGNORE_TEMPLATE);

        String text = fileTemplate.getText();
        VfsUtil.saveText(file, text);
    }

    private VirtualFile createPomFile(Project project, VirtualFile root, Properties properties) throws IOException {
        VirtualFile file = root.findChild(MavenConstants.POM_XML);
        if (file != null && file.exists()) {
            LOG.info("File '" + MavenConstants.POM_XML + "' already exits");
            return file;
        }

        file = root.createChildData(this, MavenConstants.POM_XML);

        FileTemplateManager ftManager = FileTemplateManager.getInstance(project);
        FileTemplate fileTemplate = ftManager.getTemplate(MidPointFileTemplateGroupFactory.MIDPOINT_MAVEN_POM_TEMPLATE);

        String text = fileTemplate.getText(properties);
        Pattern pattern = Pattern.compile("\\$\\$\\{(.*)\\}");
        Matcher matcher = pattern.matcher(text);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(builder, "\\$" + matcher.group(1).toUpperCase() + "\\$");
        }
        matcher.appendTail(builder);
        text = builder.toString();

        TemplateImpl template = (TemplateImpl) TemplateManager.getInstance(project).createTemplate("", "", text);
        for (int i = 0; i < template.getSegmentsCount(); i++) {
            if (i == template.getEndSegmentNumber()) {
                continue;
            }
            String name = template.getSegmentName(i);
            String value = "\"" + properties.getProperty(name, "") + "\"";
            template.addVariable(name, value, value, true);
        }

        VfsUtil.saveText(file, template.getTemplateText());

        // todo reformat pom file
//        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
//        if (psiFile != null) {
//            new ReformatCodeProcessor(project, psiFile, null, false).run();
//        }

        return file;
    }

    @Override
    public ModuleType<?> getModuleType() {
        return StdModuleTypes.JAVA;
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new MidPointWizardStep(settings);
    }

    @Nullable
    @Override
    public Module commitModule(@NotNull Project project, @Nullable ModifiableModuleModel model) {
        MidPointService.getInstance(project).setSettings(settings.getMidPointSettings());

        EncryptionService.getInstance(project).init(settings.getMasterPassword());

        EnvironmentService.getInstance(project).setSettings(settings.getEnvironmentSettings());

        return super.commitModule(project, model);
    }
}
