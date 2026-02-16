package com.evolveum.midpoint.studio.action.smart.suggestion;

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
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.dialog.GenerateSuggestionDialogContext;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.dialog.GenerateSuggestionWizard;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionsEditor;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionsRenderer;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.model.SmartSuggestionTableModel;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.evolveum.midpoint.studio.ui.treetable.FilterableColumnInfo;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectTypesSuggestionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceObjectTypeDefinitionType;
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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.*;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Created by Dominik.
 */
public class ObjectTypeSuggestionAction extends AnAction {

    private static final Logger log = Logger.getInstance(ObjectTypeSuggestionAction.class);

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

        GenerateSuggestionDialogContext generateSuggestionDialogContext = new GenerateSuggestionDialogContext();
        generateSuggestionDialogContext.setMode(GenerateSuggestionDialogContext.ResourceDialogContextMode.OBJECT_TYPE);
        generateSuggestionDialogContext.setResources(foundResources);
        generateSuggestionDialogContext.setResourceOid(uploadedResourceOid);

        new GenerateSuggestionWizard(
                project,
                "Smart suggestion - resource object type",
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

                                ObjectTypesSuggestionType objectSuggestion;

                                @Override
                                public void run(@NotNull ProgressIndicator progressIndicator) {
                                    DownloadTask downloadTask = new DownloadTask(project,
                                            List.of(new Pair<>(generateSuggestionDialogContext.getResourceOid(), ObjectTypes.RESOURCE)),
                                            false,
                                            true,
                                            true);
                                    downloadTask.setEnvironment(env);
                                    downloadTask.setOpenAfterDownload(true);
                                    ProgressManager.getInstance().run(downloadTask);

                                    objectSuggestion = client.getSuggestObjectTypes(
                                            generateSuggestionDialogContext.getResourceOid(),
                                            generateSuggestionDialogContext.getObjectClass()
                                    );
                                }

                                @Override
                                public void onFinished() {
                                    if (objectSuggestion != null) {
                                        var model = new SmartSuggestionTableModel<ResourceObjectTypeDefinitionType>(List.of(
                                                new FilterableColumnInfo<>("Name",
                                                        obj -> {
                                                    if (obj instanceof SmartSuggestionObject<?> sso) {
                                                        return ((ResourceObjectTypeDefinitionType) sso.getObject()).getDisplayName();
                                                    }
                                                    return null;
                                                }),
                                                new FilterableColumnInfo<>("Kind", obj -> {
                                                    if (obj instanceof SmartSuggestionObject<?> sso) {
                                                        return ((ResourceObjectTypeDefinitionType) sso.getObject()).getKind().value();
                                                    }
                                                    return null;
                                                }),
                                                new FilterableColumnInfo<>("Intent", obj -> {
                                                    if (obj instanceof SmartSuggestionObject<?> sso) {
                                                        return ((ResourceObjectTypeDefinitionType) sso.getObject()).getIntent();
                                                    }
                                                    return null;
                                                }),
                                                new DefaultColumnInfo<>("Description", obj -> {
                                                    if (obj instanceof SmartSuggestionObject<?> sso) {
                                                        return ((ResourceObjectTypeDefinitionType) sso.getObject()).getDescription();
                                                    }
                                                    return null;
                                                }),
                                                new DefaultColumnInfo<>("Activities") {
                                                    @Override
                                                    public @Nullable Object valueOf(DefaultMutableTreeTableNode node) {
                                                        return node.getUserObject();
                                                    }

                                                    @Override
                                                    public boolean isCellEditable(DefaultMutableTreeTableNode node) {
                                                        return true;
                                                    }

                                                    @Override
                                                    public TableCellRenderer getCustomizedRenderer(DefaultMutableTreeTableNode node, TableCellRenderer renderer) {
                                                        return new ActionsRenderer();
                                                    }

                                                    @Override
                                                    public @NotNull TableCellRenderer getRenderer(DefaultMutableTreeTableNode o) {
                                                        return new ActionsRenderer();
                                                    }

                                                    @Override
                                                    public TableCellEditor getEditor(DefaultMutableTreeTableNode o) {
                                                        return new ActionsEditor(project, prismContext);
                                                    }
                                                }
                                        ));

                                        ResourceType resource = generateSuggestionDialogContext.getResources().stream()
                                                .filter(o -> o.getOid().equals(generateSuggestionDialogContext.getResourceOid()))
                                                .filter(ResourceType.class::isInstance)
                                                .map(ResourceType.class::cast)
                                                .findFirst()
                                                .orElseThrow(
                                                        () -> new RuntimeException("Object ResourceType with oid '" + generateSuggestionDialogContext.getResourceOid() + "' not found")
                                                );

                                        model.setData(objectSuggestion.getObjectType().stream()
                                                .map(o -> new SmartSuggestionObject<>(o, resource))
                                                .toList());

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

                                        contentManager.addContent(ContentFactory.getInstance().createContent(
                                                panel ,
                                                "Resource Object Type",
                                                false
                                        ));
                                        toolWindow.activate(() -> {
                                            log.info("Content of tool window with ID '" + toolWindowId + "' was update");
                                        });

                                        String msg = "Generate Smart suggestion successful";
                                        log.info(msg);

                                        Notification notification = new Notification(
                                                "midpointSmartSuggestion",
                                                "Midpoint Smart suggestion",
                                                msg,
                                                NotificationType.INFORMATION
                                        );
                                        Notifications.Bus.notify(notification, project);
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
}
