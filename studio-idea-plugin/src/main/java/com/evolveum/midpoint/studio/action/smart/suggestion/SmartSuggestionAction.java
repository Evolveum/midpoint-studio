package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.alert.DialogAlert;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionDialogContext;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionWizard;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.model.SmartSuggestionTableModel;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.ide.util.treeView.TreeState;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
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
import java.util.List;
import java.util.Objects;

public abstract class SmartSuggestionAction<T> extends AnAction {

    private String resourceOid;

    abstract boolean isLockable();
    abstract boolean getPresentation(PsiFile psiFile);
    abstract GenerateSuggestionDialogContext.ResourceDialogContextMode getModeDialogContext();
    abstract Logger getLogger();
    abstract SmartSuggestionTableModel<T> getModel(Project project, PrismContext prismContext);
    abstract List<SmartSuggestionObject<T>> getSuggestions(
            MidPointClient client,
            GenerateSuggestionDialogContext generateSuggestionDialogContext
    );

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
        EnvironmentService em = EnvironmentService.getInstance(Objects.requireNonNull(anActionEvent.getProject()));
        Environment env = em.getSelected();

        if (resourceOid != null) {
            new DialogAlert(
                    anActionEvent.getProject(),
                    "Upload (Full Processing)",
                    "The local resource configuration (OID: '" + resourceOid + "') will be uploaded to midPoint to generate AI suggestions based on the most recent data.",
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

    private void showSelectResourceDialogWindow(
            @NotNull AnActionEvent anActionEvent,
            Environment env,
            String uploadedResourceOid
    ) {
        var log = getLogger();
        var project = anActionEvent.getProject();

        if (project == null) {
            log.error("Project is null");
            return;
        }

        var prismContext = StudioPrismContextService.getPrismContext(project);
        MidPointClient client = new MidPointClient(project, env);
        @Deprecated
        var foundResources = client.list(ObjectTypes.RESOURCE.getClassDefinition(), prismContext.queryFactory().createQuery(), true);

        GenerateSuggestionDialogContext generateSuggestionDialogContext = new GenerateSuggestionDialogContext();
        generateSuggestionDialogContext.setMode(getModeDialogContext());
        generateSuggestionDialogContext.setResources(foundResources);
        generateSuggestionDialogContext.setResourceOid(uploadedResourceOid);

        new GenerateSuggestionWizard(
                project,
                "Smart suggestion - " + getTemplatePresentation().getText(),
                generateSuggestionDialogContext,
                new DialogWindowActionHandler() {

                    @Override
                    public boolean isOkButtonEnabled() {
                        return generateSuggestionDialogContext.getResourceOid() != null &&
                                generateSuggestionDialogContext.getObjectClass() != null;
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

                                List<SmartSuggestionObject<T>> objectSuggestions = null;

                                @Override
                                public void run(@NotNull ProgressIndicator progressIndicator) {
                                    RunnableUtils.runWriteActionAndWait(() -> {
                                        var psiFileResource = MidPointUtils.findPsiByOid(project, generateSuggestionDialogContext.getResourceOid());
                                        MidPointUtils.openFile(project, psiFileResource.getVirtualFile());
                                    });

                                    objectSuggestions = getSuggestions(client, generateSuggestionDialogContext);
                                }

                                @Override
                                public void onFinished() {
                                    if (objectSuggestions != null) {
                                        var model = getModel(project, prismContext);
                                        model.setData(objectSuggestions);
                                        contentManager.addContent(ContentFactory.getInstance().createContent(
                                                createTablePanel(model),
                                                getTemplatePresentation().getText(),
                                                isLockable()
                                        ));

                                        toolWindow.activate(() -> {
                                            log.info("Content of tool window with ID '" + toolWindowId + "' was update");
                                        });

                                        Notification notification = new Notification(
                                                "midpointSmartSuggestion",
                                                "Midpoint Smart suggestion",
                                                "Generate Smart suggestion successful",
                                                NotificationType.INFORMATION
                                        );
                                        Notifications.Bus.notify(notification, project);
                                        log.info(notification.getContent());
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
                }
        ).show();
    }

    protected ResourceType getResources(GenerateSuggestionDialogContext generateSuggestionDialogContext) {
        return generateSuggestionDialogContext.getResources().stream()
                .filter(o -> o.getOid().equals(generateSuggestionDialogContext.getResourceOid()))
                .filter(ResourceType.class::isInstance)
                .map(ResourceType.class::cast)
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException("Object ResourceType with oid '" + generateSuggestionDialogContext.getResourceOid() + "' not found")
                );
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
