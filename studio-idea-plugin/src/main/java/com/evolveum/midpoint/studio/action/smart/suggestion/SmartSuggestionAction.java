/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionDataModel;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionWizard;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.model.SmartSuggestionTableModel;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.ide.util.treeView.TreeState;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public abstract class SmartSuggestionAction<T> extends AnAction {

    public final LocalizationService localizationService = new LocalizationService();

    private final String TITLE = "Midpoint Smart suggestion";

    private String resourceOid;

    abstract Logger getLogger();

    abstract boolean isLockable();

    abstract boolean getPresentation(PsiFile psiFile);

    abstract GenerateSuggestionDataModel.ResourceDialogContextMode getModeDialogContext();

    abstract SmartSuggestionTableModel<T> getModel(Project project, PrismContext prismContext);

    abstract String submitOperation(
            MidPointClient client,
            GenerateSuggestionDataModel dataModel
    ) throws SchemaException, AuthenticationException, IOException;

    abstract SmartIntegrationOperationStatusInfoType getStatusInfo(
            MidPointClient client,
            String token
    ) throws SchemaException, AuthenticationException, IOException;

    abstract List<SmartSuggestionObject<T>> getResultSuggestions(
            AbstractSmartIntegrationOperationResultType result,
            GenerateSuggestionDataModel dataModel
    ) throws SchemaException;

    public @Nullable String getResourceOid() {
        return resourceOid;
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        var presentation = anActionEvent.getPresentation();
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);

        if (psiFile != null) {
            resourceOid = MidPointUtils.findResourceOidByPsi(psiFile);
            presentation.setEnabled(getPresentation(psiFile));
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        var project = anActionEvent.getProject();
        EnvironmentService em = EnvironmentService.getInstance(Objects.requireNonNull(project));
        Environment env = em.getSelected();
        MidPointClient client = new MidPointClient(project, env);

        if (resourceOid != null && !resourceOid.isEmpty()) {

            new DialogWrapper(
                    anActionEvent.getProject()
            ) {

                {
                    setTitle("Upload (Full Processing)");
                    setSize(600, 50);
                    init();
                }

                @Override
                protected JComponent createCenterPanel() {

                    JBLabel description = new JBLabel(
                            "<html>" +
                                    "The local resource configuration (OID: %s) ".formatted(resourceOid) +
                                    "will be uploaded to midPoint to generate AI suggestions based on the most recent data." +
                                    "</html>"
                    );

                    JPanel panel = new JPanel(new BorderLayout(0, 10));
                    panel.add(description, BorderLayout.NORTH);

                    return panel;
                }

                @Override
                protected void doOKAction() {
                    new UploadFullProcessingTask(
                            anActionEvent.getProject(), anActionEvent::getDataContext, env
                    ) {
                        @Override
                        public void onFinished() {
                            if (!hasFailures()) {
                                ApplicationManager.getApplication().invokeLater(() ->
                                        showSelectResourceDialogWindow(anActionEvent, client, resourceOid));
                            }
                        }
                    }.queue();

                    super.doOKAction();
                }

            }.show();

        } else {
            ApplicationManager.getApplication().invokeLater(() ->
                    showSelectResourceDialogWindow(anActionEvent, client, null));
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private void showSelectResourceDialogWindow(
            @NotNull AnActionEvent anActionEvent,
            MidPointClient client,
            String uploadedResourceOid
    ) {
        var log = getLogger();
        var project = anActionEvent.getProject();

        if (project == null) {
            log.error("Project is null");
            return;
        }

        var prismContext = StudioPrismContextService.getPrismContext(project);

        @Deprecated
        var foundResources = client.list(
                ObjectTypes.RESOURCE.getClassDefinition(),
                prismContext.queryFactory().createQuery(),
                true
        );

        GenerateSuggestionDataModel dataModel = new GenerateSuggestionDataModel();
        dataModel.setMode(getModeDialogContext());
        dataModel.setResources(foundResources);
        dataModel.setResourceOid(uploadedResourceOid);
        dataModel.setAiInfo(client.getAiInfo());

        new GenerateSuggestionWizard(
            project,
            TITLE + " - " + getTemplatePresentation().getText(),
            dataModel,
            () -> {
                String toolWindowId = "SmartSuggestionToolWindow";
                ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(toolWindowId);

                if (toolWindow != null) {
                    var contentManager = toolWindow.getContentManager();
                    contentManager.removeAllContents(true);

                    ProgressManager.getInstance().run(new Task.Backgroundable(
                        project,
                        "Generate suggestion",
                        true
                    ) {

                        List<SmartSuggestionObject<T>> objectSuggestions = null;

                        @Override
                        public void run(@NotNull ProgressIndicator progressIndicator) {
                            RunnableUtils.runWriteActionAndWait(() -> {
                                var psiFileResource = MidPointUtils.findPsiByOid(project, dataModel.getResourceOid());
                                MidPointUtils.openFile(
                                    project,
                                    psiFileResource != null ? psiFileResource.getVirtualFile() : null
                                );
                            });

                            try {

                                // FIXME check if action down if scheduler stoped????
                                generateSuggestions(client, dataModel).whenComplete((
                                        result, ex
                                ) -> {
                                    if (ex != null) {
                                        log.error("Couldn't generate suggestions", ex);
                                        MidPointUtils.publishNotification(
                                            project,
                                            "midpointSmartSuggestion",
                                            TITLE,
                                            "Couldn't generate suggestions",
                                            NotificationType.ERROR
                                        );
                                    } else {
                                        objectSuggestions = result;
                                    }
                                });
                            } catch (Exception e) {

                                log.error(e);
                                MidPointUtils.publishNotification(
                                    project,
                                    "midpointSmartSuggestion",
                                    TITLE,
                                    e.getMessage(),
                                    NotificationType.ERROR
                                );
                            }
                        }

                        @Override
                        public void onFinished() {

                            if (objectSuggestions != null && !objectSuggestions.isEmpty()) {
                                var model = getModel(project, prismContext);
                                model.setData(objectSuggestions);
                                contentManager.addContent(ContentFactory.getInstance().createContent(
                                        createTablePanel(model),
                                        getTemplatePresentation().getText(),
                                        isLockable()
                                ));

                                toolWindow.activate(() ->
                                        log.info("Content of tool window with ID '" + toolWindowId + "' was update"));

                                var infoMsg = "Generate Smart suggestion successful";

                                log.info(infoMsg);
                                MidPointUtils.publishNotification(
                                        project,
                                        "midpointSmartSuggestion",
                                        TITLE,
                                        infoMsg,
                                        NotificationType.INFORMATION
                                );
                            } else {
                                JLabel errorLabel = new JLabel("Suggestion not found");
                                errorLabel.setForeground(JBColor.RED);
                                errorLabel.setBorder(JBUI.Borders.empty(10, 15));
                                contentManager.addContent(ContentFactory.getInstance().createContent(
                                        errorLabel, "Smart Suggestion", false));
                                log.warn(errorLabel.getText());
                            }
                        }
                    });
                } else {
                    log.error("Tool window with ID '" + toolWindowId + "' not found!");
                }
            }
        ).show();
    }

    private CompletableFuture<List<SmartSuggestionObject<T>>> generateSuggestions(
            MidPointClient client,
            GenerateSuggestionDataModel dataModel
    ) throws SchemaException, AuthenticationException, IOException {

        CompletableFuture<List<SmartSuggestionObject<T>>> future =
                new CompletableFuture<>();

        if (dataModel.getResourceOid() == null || dataModel.getObjectClass() == null) {
            future.complete(List.of());
            return future;
        }

        String token = submitOperation(client, dataModel);

        if (token == null) {
            future.completeExceptionally(
                    new IllegalStateException("Submit operation failed. Token is null.")
            );
            return future;
        }

        var scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                var statusInfo = getStatusInfo(client, token);


                if (OperationResultStatusType.IN_PROGRESS.equals(statusInfo.getStatus())) {
                    return;
                }

                if (OperationResultStatusType.SUCCESS.equals(statusInfo.getStatus())) {
                    future.complete(
                            getResultSuggestions(statusInfo.getResult(), dataModel)
                    );
                } else {
                    future.completeExceptionally(
                            new RuntimeException(
                                    "Task finished with status: " + statusInfo
                            )
                    );
                }

                scheduler.shutdown();

            } catch (Exception e) {
                getLogger().error(e);
                future.completeExceptionally(e);
                scheduler.shutdown();
            }
        }, 0, 2, TimeUnit.SECONDS);

        return future;
    }

    protected ResourceType getResources(GenerateSuggestionDataModel dataModel) {
        return dataModel.getResources().stream()
                .filter(o -> o.getOid().equals(dataModel.getResourceOid()))
                .filter(ResourceType.class::isInstance)
                .map(ResourceType.class::cast)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Object ResourceType with oid '" +
                        dataModel.getResourceOid() + "' not found"));
    }

    private JPanel createTablePanel(SmartSuggestionTableModel<?> model) {
        var table = new DefaultTreeTable<>(model);
        table.setShowColumns(true);
        table.setRootVisible(false);
        table.setDragEnabled(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(50);

        SearchTextField searchTextField = new SearchTextField();
        searchTextField.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                TreeState state = TreeState.createOn(table.getTree());
                model.applyFilter(searchTextField.getText());
                TreeUtil.expandAll(table.getTree());
                state.applyTo(table.getTree());
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(searchTextField, BorderLayout.NORTH);
        panel.add(new JBScrollPane(table), BorderLayout.CENTER);

        return panel;
    }
}
