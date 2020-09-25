package com.evolveum.midpoint.studio.impl.ide;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.ui.MidPointWizardStep;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointModuleBuilder extends ModuleBuilder {

    public static final String NOTIFICATION_KEY = "Module";

    public static String MODULE_NAME = "MidPoint";

    private ProjectSettings settings = new ProjectSettings();

    public MidPointModuleBuilder() {
        setName(MODULE_NAME);
    }

    @Override
    public String getBuilderId() {
        return getClass().getName();
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
        return MidPointIcons.ACTION_MIDPOINT;
    }

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) {
        VirtualFile root = createAndGetContentEntry();
        modifiableRootModel.addContentEntry(root);

        try {
            VfsUtil.createDirectories(root.getPath() + "/objects");
            VfsUtil.createDirectories(root.getPath() + "/scratches");
        } catch (IOException ex) {
            MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, "Couldn't create directory structure", ex);
        }

        // build pom file
        Project project = modifiableRootModel.getProject();
        MidPointUtils.runWhenInitialized(project, (DumbAwareRunnable) () -> {

            Application am = ApplicationManager.getApplication();

            if (!am.isDispatchThread()) {
                am.invokeLater(() -> WriteAction.run(() -> createProjectFiles(project, root)));
                return;
            }

            WriteAction.run(() -> createProjectFiles(project, root));
        });
        createProjectFiles(project, root);

        FacetType facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
        FacetManager.getInstance(modifiableRootModel.getModule()).addFacet(facetType, facetType.getDefaultFacetName(), null);
    }

    public void createProjectFiles(Project project, VirtualFile root) {
        try {
            Properties properties = new Properties();
            properties.setProperty("GROUP_ID", root.getName());
            properties.setProperty("ARTIFACT_ID", root.getName());
            properties.setProperty("VERSION", "0.1-SNAPSHOT");

            createPomFile(project, root, properties);

            createGitIgnoreFile(project, root);
        } catch (IOException ex) {
            MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, "Couldn't create pom.xml file", ex);
        }
    }

    private void createGitIgnoreFile(Project project, VirtualFile root) throws IOException {
        FileTemplateManager ftManager = FileTemplateManager.getInstance(project);
        FileTemplate fileTemplate = ftManager.getTemplate(MidPointFileTemplateGroupFactory.MIDPOINT_GIT_IGNORE_TEMPLATE);

        VirtualFile file = root.createChildData(this, ".gitignore");

        String text = fileTemplate.getText();
        VfsUtil.saveText(file, text);
    }

    private void createPomFile(Project project, VirtualFile root, Properties properties) throws IOException {
        VirtualFile file = root.createChildData(this, MavenConstants.POM_XML);

        FileTemplateManager ftManager = FileTemplateManager.getInstance(project);
        FileTemplate fileTemplate = ftManager.getTemplate(MidPointFileTemplateGroupFactory.MIDPOINT_MAVEN_POM_TEMPLATE);

        String text = fileTemplate.getText(properties);
        Pattern pattern = Pattern.compile("\\$\\$\\{(.*)\\}");
        Matcher matcher = pattern.matcher(text);
        StringBuffer builder = new StringBuffer();
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

        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {
            new ReformatCodeProcessor(project, psiFile, null, false).run();
        }

        MavenProjectsManager mpManager = MavenProjectsManager.getInstance(project);
        mpManager.addManagedFilesOrUnignore(Collections.singletonList(file));
    }

    @Override
    public ModuleType getModuleType() {
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

    private VirtualFile createAndGetContentEntry() {
        String path = FileUtil.toSystemIndependentName(getContentEntryPath());

        new File(path).mkdirs();

        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
    }
}
