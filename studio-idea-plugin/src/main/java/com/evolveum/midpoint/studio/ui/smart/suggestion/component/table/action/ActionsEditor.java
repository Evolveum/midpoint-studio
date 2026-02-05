package com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.action;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.SmartSuggestionObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceObjectTypeDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaHandlingType;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.comparison.ComparisonManager;
import com.intellij.diff.comparison.ComparisonPolicy;
import com.intellij.diff.fragments.LineFragment;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.diff.util.Side;
import com.intellij.icons.AllIcons;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class ActionsEditor extends AbstractCellEditor implements TableCellEditor {

    private final Project project;
    private final PrismContext prismContext;
    private final ActionPanel panel;

    private JTable table;
    private SmartSuggestionObject<?> value;
    private boolean isSelected;
    private int row;
    private int column;

    public ActionsEditor(Project project, PrismContext prismContext) {
        this.project = project;
        this.prismContext = prismContext;
        panel = new ActionPanel();

        panel.getApply().addActionListener(e -> {
            apply();
            fireEditingStopped();
            stopCellEditing();
        });

        panel.getDiscard().addActionListener(e -> {
            discard();
            fireEditingStopped();
            stopCellEditing();
        });

        panel.getDetails().addActionListener(e -> {
            showDetails();
            fireEditingStopped();
            stopCellEditing();
        });
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value,
            boolean isSelected, int row, int column) {
        this.table = table;
        this.value = (SmartSuggestionObject<?>) value;
        this.isSelected = isSelected;
        this.row = row;
        this.column = column;

        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return value;
    }

    private void apply() {
        try {
            PsiFile psiFile = MidPointUtils.findPsiByOid(project, value.getResourceOid());
            PrismObject<?> po = prismContext.parserFor(psiFile.getText()).parse();

            if (po.asObjectable() instanceof ResourceType resource) {
                if (value.getObject() instanceof ResourceObjectTypeDefinitionType resourceObjectTypeDefinitionType) {
                    if (resource.getSchemaHandling() == null) {
                        resource.setSchemaHandling(new SchemaHandlingType());
                    }
                    resource.getSchemaHandling().getObjectType().add(resourceObjectTypeDefinitionType.clone());
                }

                updateContentOfEditor(psiFile, prismContext.serializerFor(PrismContext.LANG_XML).serializeRealValue(resource));
            } else {
                MidPointUtils.publishNotification(
                        project,
                        "Object not found",
                        "Object not found",
                        "Object with oid '"  + value.getResourceOid() + " 'not found",
                        NotificationType.ERROR
                );
            }
        } catch (SchemaException ex) {
            throw new RuntimeException("Failed to parse object", ex);
        }
    }

    private void discard() {
        // TODO discard action impl
    }

    private void showDetails() {
        String rawXml;

        try {
            rawXml = prismContext.serializerFor(PrismContext.LANG_XML).serializeRealValue(value.getObject());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to serialize object", ex);
        }

        var smartEditor = new SmartEditorComponent(project, XMLLanguage.INSTANCE, "");
        WriteCommandAction.runWriteCommandAction(project, () -> {
            smartEditor.setText(rawXml != null ? rawXml : "");
        });

        Rectangle rect = table.getCellRect(row, 0, true);
        Point point = new Point(rect.x, rect.y + rect.height);

        JBPanel<?> popupRoot = new JBPanel<>(new BorderLayout());
        popupRoot.removeAll();
        popupRoot.add(smartEditor, BorderLayout.CENTER);
        popupRoot.setBorder(JBUI.Borders.empty(1));
        popupRoot.setPreferredSize(new Dimension(table.getWidth(),300));
        popupRoot.revalidate();
        popupRoot.repaint();

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(popupRoot, popupRoot)
                .setFocusable(true)
                .setRequestFocus(false)
                .setResizable(true)
                .setMovable(false)
                .setCancelOnClickOutside(true)
                .createPopup();

        popup.show(new RelativePoint(table, point));
    }

    private void updateContentOfEditor(@NotNull PsiFile originalPsiFile, String updated) {
        AnAction acceptAction = new AnAction("Accept Suggestion", "Accept smart suggestion", AllIcons.Actions.ShowWriteAccess) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    Document document = originalPsiFile.getViewProvider().getDocument();
                    if (document != null) {
                        document.setText(updated);
                    }
                });
            }
        };

        SimpleDiffRequest diffRequest = new SimpleDiffRequest(
            "Preview Changes",
            DiffContentFactory.getInstance().create(project, originalPsiFile.getText(), originalPsiFile.getFileType()),
            DiffContentFactory.getInstance().create(project, updated, originalPsiFile.getFileType()),
            "Original",
            "Suggested"
        );

        diffRequest.putUserData(
                DiffUserDataKeys.CONTEXT_ACTIONS,
                List.of(acceptAction)
        );

        List<LineFragment> fragments = ComparisonManager.getInstance()
                .compareLines(originalPsiFile.getText(), updated, ComparisonPolicy.DEFAULT, new EmptyProgressIndicator());
        fragments.stream()
                .sorted(Comparator.comparingInt(LineFragment::getStartLine2))
                .skip(1)
                .findFirst()
                .ifPresent(fragment ->
                        diffRequest.putUserData(
                                DiffUserDataKeys.SCROLL_TO_LINE,
                                Pair.create(Side.RIGHT, fragment.getStartLine2())
                        )
                );

        DiffManager.getInstance().showDiff(project, diffRequest);
    }
}