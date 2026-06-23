package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.ui.dialog.alert.DialogAlert;
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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.SearchTextField;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public abstract class SmartSuggestionAction<T> extends AnAction {

    private final String TITLE = "Midpoint Smart suggestion";

    private String resourceOid;

    abstract Logger getLogger();

    abstract boolean isLockable();

    abstract boolean getPresentation(PsiFile psiFile);

    abstract GenerateSuggestionDataModel.ResourceDialogContextMode getModeDialogContext();

    abstract SmartSuggestionTableModel<T> getModel(Project project, PrismContext prismContext);

    abstract String submitOperation(
            MidPointClient client,
            GenerateSuggestionDataModel resourceOid
    ) throws SchemaException, AuthenticationException, IOException;

    abstract SmartIntegrationOperationStatusInfoType getStatusInfo(
            MidPointClient client,
            String token
    ) throws SchemaException, AuthenticationException, IOException;

    abstract List<SmartSuggestionObject<T>> getResultSuggestions(
            AbstractSmartIntegrationOperationResultType result,
            GenerateSuggestionDataModel model
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

            new DialogAlert(
                    anActionEvent.getProject(),
                    "Upload (Full Processing)",
                    "The local resource configuration (OID: '" + resourceOid + "') will be uploaded to midPoint to generate AI suggestions based on the most recent data.",
                    () -> ProgressManager.getInstance().run(
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
                            }
                    )
            ).show();
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
        var foundResources = client.list(ObjectTypes.RESOURCE.getClassDefinition(), prismContext.queryFactory().createQuery(), true);

        GenerateSuggestionDataModel dataModel = new GenerateSuggestionDataModel();
        dataModel.setMode(getModeDialogContext());
        dataModel.setResources(foundResources);
        dataModel.setResourceOid(uploadedResourceOid);

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
            GenerateSuggestionDataModel model
    ) throws SchemaException, AuthenticationException, IOException {
        CompletableFuture<List<SmartSuggestionObject<T>>> future =
                new CompletableFuture<>();

        if (model.getResourceOid() == null || model.getObjectClass() == null) {
            future.complete(List.of());
            return future;
        }

        String token = submitOperation(client, model);

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

                if (statusInfo.getStatus().equals(OperationResultStatusType.IN_PROGRESS) ||
                        statusInfo.getStatus().equals(OperationResultStatusType.UNKNOWN)
                ) {
                    return;
                }

                if (statusInfo.getStatus().equals(OperationResultStatusType.SUCCESS)) {
                    future.complete(getResultSuggestions(statusInfo.getResult(), model));
                    scheduler.shutdown();
                }

                if (!statusInfo.getStatus().equals(OperationResultStatusType.UNKNOWN)) {
                    future.completeExceptionally(
                        new RuntimeException("Task finished with status: " + statusInfo)
                    );
                    scheduler.shutdown();
                }
            } catch (Exception e) {
                getLogger().error(e);
                future.completeExceptionally(e);
                scheduler.shutdown();
            }
        }, 0, 2, TimeUnit.SECONDS);

        return future;
    }

    protected ResourceType getResources(GenerateSuggestionDataModel generateSuggestionDataModel) {
        return generateSuggestionDataModel.getResources().stream()
                .filter(o -> o.getOid().equals(generateSuggestionDataModel.getResourceOid()))
                .filter(ResourceType.class::isInstance)
                .map(ResourceType.class::cast)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Object ResourceType with oid '" +
                        generateSuggestionDataModel.getResourceOid() + "' not found"));
    }

    private JPanel createTablePanel(SmartSuggestionTableModel<?> model) {
        var table = new DefaultTreeTable<>(model);
        table.setShowColumns(true);
        table.setRootVisible(false);
        table.setDragEnabled(false);
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
