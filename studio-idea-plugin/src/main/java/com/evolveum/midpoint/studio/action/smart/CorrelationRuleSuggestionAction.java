/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.action.smart;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.task.DownloadTask;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.alert.DialogAlert;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.ResourceDialogContext;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.ResourceObjectTypeWizard;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.correlation.CorrelationRuleSuggestionList;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CorrelationSuggestionsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
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

public class CorrelationRuleSuggestionAction extends AnAction {

    private static final Logger log = Logger.getInstance(ResourceObjectTypeSuggestionAction.class);

    private String resourceOid;

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        var presentation = anActionEvent.getPresentation();
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);

        if (psiFile != null) {
            resourceOid = MidPointUtils.findResourceOidByPsi(psiFile);
            presentation.setEnabled(resourceOid != null && isCorrelationRule(psiFile));
        } else {
            resourceOid = null;
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Environment env = EnvironmentService.getInstance(Objects.requireNonNull(anActionEvent.getProject())).getSelected();

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
                                        showSelectResourceDialogWindow(anActionEvent, resourceOid, env);
                                    }
                                }
                            });
                        }
                    }
            ).show();
        } else {
            showSelectResourceDialogWindow(anActionEvent,null, env);
        }
    }

    private void showSelectResourceDialogWindow(@NotNull AnActionEvent anActionEvent, String uploadedResourceOid, Environment env) {
        var project = anActionEvent.getProject();

        if (project == null) {
            log.error("Project is null");
            return;
        }

        MidPointClient client = new MidPointClient(anActionEvent.getProject(), env);
        PrismContext prismContext = StudioPrismContextService.getPrismContext(anActionEvent.getProject());

        @Deprecated
        var foundResources = client.list(ObjectTypes.RESOURCE.getClassDefinition(), prismContext.queryFactory().createQuery(), true);

        ResourceDialogContext resourceDialogContext = new ResourceDialogContext();
        resourceDialogContext.setMode(ResourceDialogContext.ResourceDialogContextMode.CORRELATION);
        resourceDialogContext.setResources(foundResources);
        resourceDialogContext.setResourceOid(uploadedResourceOid);

        new ResourceObjectTypeWizard(
                project,
                "Smart suggestion - correlation rule",
                resourceDialogContext,
                new DialogWindowActionHandler() {

                    @Override
                    public boolean isOkButtonEnabled() {
                        return resourceDialogContext.getResourceOid() != null &&
                                resourceDialogContext.getObjectType() != null;
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

                            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generate suggestion", true) {

                                CorrelationSuggestionsType correlationSuggestions;

                                @Override
                                public void run(@NotNull ProgressIndicator progressIndicator) {
                                    correlationSuggestions = client.getSuggestCorrelationRule(
                                            resourceDialogContext.getResourceOid(),
                                            resourceDialogContext.getObjectType().getKind().value(),
                                            resourceDialogContext.getObjectType().getIntent()
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
                                    if (correlationSuggestions != null) {
                                        contentManager.addContent(ContentFactory.getInstance().createContent(
                                                new CorrelationRuleSuggestionList(
                                                        project,
                                                        prismContext,
                                                        resourceDialogContext.getResources().stream()
                                                                .filter(o -> o instanceof ResourceType)
                                                                .map(o -> (ResourceType) o)
                                                                .filter(r -> resourceDialogContext.getResourceOid().equals(r.getOid()))
                                                                .findFirst()
                                                                .orElse(null),
                                                        resourceDialogContext.getObjectType(),
                                                        correlationSuggestions), "Correlations Suggestion", false));
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

    private boolean isCorrelationRule(PsiFile psiFile) {
        if (!(psiFile instanceof XmlFile)) {
            return false;
        }

        XmlTag root = ((XmlFile) psiFile).getRootTag();
        if (root == null) {
            return false;
        }

        XmlTag[] schemaHandlingTags = root.findSubTags("schemaHandling");
        if (schemaHandlingTags.length == 0) {
            return false;
        }

        for (XmlTag schema : schemaHandlingTags) {
            if (schema.findFirstSubTag("objectType") != null) {
                return true;
            }
        }

        return false;
    }
}
