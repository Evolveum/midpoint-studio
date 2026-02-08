package com.evolveum.midpoint.studio.action.smart.suggestion;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.util.ResourceTypeUtil;
import com.evolveum.midpoint.studio.action.task.DownloadTask;
import com.evolveum.midpoint.studio.action.task.UploadFullProcessingTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.alert.DialogAlert;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.SmartSuggestionObject;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionsEditor;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionsRenderer;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.dialog.GenerateSuggestionDialogContext;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.dialog.GenerateSuggestionWizard;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.model.SmartSuggestionTableModel;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.List;
import java.util.Objects;

public class AssociationSuggestionAction extends AnAction {

    private static final Logger log = Logger.getInstance(AssociationSuggestionAction.class);

    private String resourceOid;

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        var presentation = anActionEvent.getPresentation();
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);

        if (psiFile != null) {
            resourceOid = MidPointUtils.findResourceOidByPsi(psiFile);
            presentation.setEnabled(resourceOid != null && associationAllowed(psiFile));
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
                "Smart suggestion - association type",
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

                                AssociationsSuggestionType associationSuggestion;

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

                                    associationSuggestion = client.getSuggestAssociations(
                                            generateSuggestionDialogContext.getResourceOid()
                                    );
                                }

                                @Override
                                public void onFinished() {
                                    if (associationSuggestion != null) {
                                        var model = new SmartSuggestionTableModel<AssociationSuggestionType>(List.of(
                                                new DefaultColumnInfo<>("Name", obj -> {
                                                    if (obj instanceof SmartSuggestionObject<?> sso) {
                                                        return ((AssociationSuggestionType) sso.getObject()).getDefinition().getDisplayName();
                                                    }
                                                    return null;
                                                }),
                                                new DefaultColumnInfo<>("Type of suggestion", obj -> {
                                                    if (obj instanceof SmartSuggestionObject<?> sso) {
                                                        // FIXME find out when is AI or system suggestion
                                                        return "System suggestion";
                                                    }
                                                    return null;
                                                }),
                                                new DefaultColumnInfo<>("Subject", obj -> {
                                                    if (obj instanceof SmartSuggestionObject<?> sso) {
                                                        var subjectList = ((AssociationSuggestionType) sso.getObject()).getDefinition().getSubject().getObjectType();
                                                        var subject = subjectList != null && !subjectList.isEmpty() ? subjectList.get(0) : null;
                                                        var subjectTypeDefinition = subject != null
                                                                ? ResourceTypeUtil.findObjectTypeDefinition(sso.getResource().asPrismObject(), subject.getKind(), subject.getIntent())
                                                                : null;
                                                        if (subjectTypeDefinition != null) {
                                                            return subjectTypeDefinition.getDisplayName() + " - " + subjectTypeDefinition.getDelineation().getObjectClass();
                                                        }
                                                    }
                                                    return null;
                                                }),
                                                new DefaultColumnInfo<>("Object", obj -> {
                                                    if (obj instanceof SmartSuggestionObject<?> sso) {
                                                        var objectList = ((AssociationSuggestionType) sso.getObject()).getDefinition().getObject();
                                                        var object = (objectList != null && !objectList.isEmpty()
                                                                && objectList.get(0).getObjectType() != null
                                                                && !objectList.get(0).getObjectType().isEmpty())
                                                                ? objectList.get(0).getObjectType().get(0)
                                                                : null;
                                                        var objectTypeDefinition = object != null
                                                                ? ResourceTypeUtil.findObjectTypeDefinition(sso.getResource().asPrismObject(), object.getKind(), object.getIntent())
                                                                : null;

                                                        if (objectTypeDefinition != null) {
                                                            return objectTypeDefinition.getDisplayName() + " - " + objectTypeDefinition.getDelineation().getObjectClass();
                                                        }
                                                    }
                                                    return null;
                                                }),
                                                new DefaultColumnInfo<>("Association data object", obj -> {
                                                    if (obj instanceof SmartSuggestionObject<?> sso) {
                                                        var association = ((AssociationSuggestionType) sso.getObject()).getDefinition().getAssociationObject();

                                                        if (association == null || association.getDelineation() == null || association.getDelineation().getObjectClass() == null) {
                                                            return null;
                                                        }
                                                        var associationObjectClass = association.getDelineation().getObjectClass();
                                                        return association.getDisplayName() + " - " + (associationObjectClass != null ? associationObjectClass.getLocalPart() : "");
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

                                        model.setData(associationSuggestion.getAssociation().stream()
                                                .map(o -> new SmartSuggestionObject<>(o, resource))
                                                .toList());

                                        var table = new DefaultTreeTable<>(model);
                                        table.setShowColumns(true);
                                        table.setRootVisible(false);
                                        table.setDragEnabled(false);
                                        table.setRowHeight(50);

                                        contentManager.addContent(ContentFactory.getInstance().createContent(
                                                new JBScrollPane(table),
                                                "Association Suggestion",
                                                false
                                        ));
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

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private boolean associationAllowed(PsiFile psiFile) {
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
