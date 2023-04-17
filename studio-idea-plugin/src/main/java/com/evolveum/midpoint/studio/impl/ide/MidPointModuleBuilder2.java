package com.evolveum.midpoint.studio.impl.ide;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.ThrowableComputable;
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
import org.jetbrains.idea.maven.wizards.MavenWizardBundle;

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
public class MidPointModuleBuilder2 extends AbstractMavenModuleBuilder {

    private static final Logger LOG = Logger.getInstance(MidPointModuleBuilder2.class);

    private static final String MODULE_NAME = "Midpoint 2";

    public MidPointModuleBuilder2() {
        setName(MODULE_NAME);
    }

    @Override
    public Icon getNodeIcon() {
        return MidPointIcons.Midpoint;
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
    public @Nullable ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return null;
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[0];
    }

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel root) {
        Project project = root.getProject();

        MavenUtil.runWhenInitialized(project, (DumbAwareRunnable) () -> setupMavenStructure(project, root));
    }

    private void setupMavenStructure(Project project, ModifiableRootModel root) {
        String contentPath = getContentEntryPath();
        String path = FileUtil.toSystemIndependentName(contentPath);

        try {
            Files.createDirectories(Path.of(path));
        } catch (IOException ex) {
            // todo error handling
            ex.printStackTrace();
        }

        VirtualFile rootFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);


        rootFile.refresh(true, false, () -> {
            try {
                var pom = WriteCommandAction.writeCommandAction(project)
                        .withName(MavenWizardBundle.message("maven.new.project.wizard.groovy.creating.groovy.project")).compute(new ThrowableComputable<VirtualFile, IOException>() {

                            @Override
                            public VirtualFile compute() throws IOException {
                                Properties properties = new Properties();

                                String escaped = rootFile.getName().replaceAll("[^a-zA-Z0-9_-]", "");

                                properties.setProperty("GROUP_ID", escaped);
                                properties.setProperty("ARTIFACT_ID", escaped);
                                properties.setProperty("VERSION", "1.0-SNAPSHOT");

                                properties.setProperty("PROJECT_NAME", escaped);


                                return createPomFile(project, rootFile, properties);
                            }
                        });

                VfsUtil.createDirectories(rootFile.getPath() + "/objects");
                VfsUtil.createDirectories(rootFile.getPath() + "/scratches");
            } catch (IOException ex) {
                // todo error handling
                ex.printStackTrace();
            }

            MavenProjectsManager mpm = MavenProjectsManager.getInstance(project);
            mpm.forceUpdateAllProjectsOrFindAllAvailablePomFiles();

            // todo handle disposable for listener
            mpm.addManagerListener(new MavenProjectsManager.Listener() {

                @Override
                public void projectImportCompleted() {
                    ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
                        try {
                            project.save();

                            ModuleManager mm = ModuleManager.getInstance(project);
                            Module module = mm.getModules()[0];

                            FacetType facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
                            FacetUtil.addFacet(module, facetType, facetType.getPresentableName());

//                                ProjectManager.getInstance().reloadProject(project);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            });

//            ApplicationManager.getApplication().executeOnPooledThread(() -> {
//                try {
//                    Promise p = mpm.waitForImportCompletion();
//
//                    p.onProcessed(o -> {
//                        ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
//                            try {
//                                project.save();
//
//                                ModuleManager mm = ModuleManager.getInstance(project);
//                                Module module = mm.getModules()[0];
//
//                                FacetType facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
//                                FacetUtil.addFacet(module, facetType, facetType.getPresentableName());
//
////                                ProjectManager.getInstance().reloadProject(project);
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
//                        });
//                    });
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            });
        });
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

//        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
//        if (psiFile != null) {
//            new ReformatCodeProcessor(project, psiFile, null, false).run();
//        }

        return file;
    }
}
