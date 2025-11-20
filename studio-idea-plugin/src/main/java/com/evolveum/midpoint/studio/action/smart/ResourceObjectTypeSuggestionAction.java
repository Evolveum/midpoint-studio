package com.evolveum.midpoint.studio.action.smart;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.task.DownloadTask;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;

import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.alert.DialogAlert;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.resource.ObjectTypeSuggestionTable;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.ResourceDialogContext;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.ResourceObjectTypeWizard;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectTypesSuggestionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.JBColor;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

/**
 * Created by Dominik.
 */
public class ResourceObjectTypeSuggestionAction extends AnAction {

    private static final Logger log = Logger.getInstance(ResourceObjectTypeSuggestionAction.class);

    private String resourceOid = null;

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        var presentation = anActionEvent.getPresentation();
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);

        if (psiFile != null) {
            resourceOid = MidPointUtils.findResourceOidByPsi(psiFile);

            if (resourceOid == null) {
                presentation.setEnabled(false);
            }
        } else {
            resourceOid = null;
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        EnvironmentService em = EnvironmentService.getInstance(Objects.requireNonNull(anActionEvent.getProject()));
        Environment env = em.getSelected();

        if (resourceOid != null) {
            new DialogAlert(
                anActionEvent.getProject(),
                "Upload (Full Processing)",
                "Upload resource with oid: '" + resourceOid + "'",
                new DialogWindowActionHandler() {
                    @Override
                    public void onOk() {
                        var task = new UploadFullProcessingTask(anActionEvent.getProject(), anActionEvent::getDataContext, env);
                        ProgressManager.getInstance().run(new Task.Backgroundable(anActionEvent.getProject(), "Uploading") {
                            @Override
                            public void run(@NotNull ProgressIndicator indicator) {
                                task.run(indicator);
                            }

                            @Override
                            public void onFinished() {
                                if (!task.hasFailures()) {
                                    showSelectResourceDialogWindow(anActionEvent, env, resourceOid);
                                }
                            }
                        });
                    }
                }
            ).show();
        } else {
            showSelectResourceDialogWindow(anActionEvent, env, null);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private void showSelectResourceDialogWindow(@NotNull AnActionEvent anActionEvent, Environment env, String uploadedResourceOid) {
        var project = anActionEvent.getProject();

        if (project == null) {
            log.error("Project is null");
            return;
        }

        MidPointClient client = new MidPointClient(project, env);
        PrismContext prismContext = StudioPrismContextService.getPrismContext(project);

        @Deprecated
        var foundResources = client.list(ObjectTypes.RESOURCE.getClassDefinition(), prismContext.queryFactory().createQuery(), true);

        ResourceDialogContext resourceDialogContext = new ResourceDialogContext();
        resourceDialogContext.setMode(ResourceDialogContext.ResourceDialogContextMode.OBJECT_TYPE);
        resourceDialogContext.setResources(foundResources);
        resourceDialogContext.setResourceOid(uploadedResourceOid);

        new ResourceObjectTypeWizard(
                project,
                "Smart suggestion - resource object type",
                resourceDialogContext,
                new DialogWindowActionHandler() {

                    @Override
                    public boolean isOkButtonEnabled() {
                        return resourceDialogContext.getResourceOid() != null &&
                                resourceDialogContext.getObjectClass() != null;
                    }

                    @Override
                    public String getOkButtonTitle() {
                        return "Allow and continue";
                    }

                    @Override
                    public void onOk() {
                        String toolWindowId = "SmartSuggestionToolWindow";
                        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(toolWindowId);

                        if (toolWindow != null) {
                            var contentManager = toolWindow.getContentManager();
                            contentManager.removeAllContents(true);

                            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generate suggestion...", true) {

                                ObjectTypesSuggestionType objectSuggestion;

                                @Override
                                public void run(@NotNull ProgressIndicator progressIndicator) {
                                    objectSuggestion = client.getSuggestObjectTypes(
                                            resourceDialogContext.getResourceOid(),
                                            resourceDialogContext.getObjectClass()
                                    );

                                    DownloadTask downloadTask = new DownloadTask(project,
                                            List.of(new Pair<>(resourceDialogContext.getResourceOid(), ObjectTypes.RESOURCE)),
                                            false,
                                            true,
                                            true);
                                    downloadTask.setEnvironment(env);
                                    downloadTask.setOpenAfterDownload(true);
                                    ProgressManager.getInstance().run(downloadTask);
                                }

                                @Override
                                public void onFinished() {
                                    if (objectSuggestion != null) {
                                        contentManager.addContent(ContentFactory.getInstance().createContent(
                                                new ObjectTypeSuggestionTable(
                                                        project,
                                                        prismContext,
                                                        resourceDialogContext.getResources().stream()
                                                                .filter(o -> o instanceof ResourceType)
                                                                .map(o -> (ResourceType) o)
                                                                .filter(r -> resourceDialogContext.getResourceOid().equals(r.getOid()))
                                                                .findFirst()
                                                                .orElse(null),
                                                        objectSuggestion), "Resource Object Type", false));
                                        toolWindow.activate(() -> {
                                            log.info("Content of tool window with ID '" + toolWindowId + "' was update");
                                        });
                                    } else {
                                        JLabel errorLabel = new JLabel("Object suggestion is null");
                                        errorLabel.setForeground(JBColor.RED);
                                        errorLabel.setBorder(JBUI.Borders.empty(10, 15));
                                        contentManager.addContent(ContentFactory.getInstance().createContent(
                                                errorLabel, "Smart Suggestion", false));
                                    }
                                }
                            });
                        } else {
                            log.error("Tool window with ID '" + toolWindowId + "' not found!");
                        }
                    }
                }
        ).show();
    }

    // method checks it if the content of a file is a resource object (for XML, JSON, YAML file types)
    private boolean isResourceObject(@NotNull PsiFile psiFile) {
        if (psiFile instanceof XmlFile xmlFile) {
            XmlTag rootTag = xmlFile.getRootTag();
            return rootTag != null && "resource".equals(rootTag.getName());
        } else if (psiFile instanceof JsonFile jsonFile) {
            JsonObject jsonObject = (JsonObject) jsonFile.getTopLevelValue();
            if (jsonObject == null || jsonObject.getPropertyList().isEmpty()) return false;
            JsonProperty first = jsonObject.getPropertyList().get(0);
            return "resource".equals(first.getName());
        }

        return false;
    }
}
