/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.table;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionPanel;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.action.ActionRenderer;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class ObjectTypeSuggestionTable extends JPanel {

    private final SmartEditorComponent smartEditor;
    private final JPanel detailsPanel = new JPanel(new BorderLayout());

    public ObjectTypeSuggestionTable(
            Project project,
            PrismContext prismContext,
            ResourceType resource,
            ObjectTypesSuggestionType objectTypesSuggestionType
    ) {
        setLayout(new BorderLayout());
        SuggestionTableModel model = new SuggestionTableModel();
        this.smartEditor = new SmartEditorComponent(project, XMLLanguage.INSTANCE);

        for (ResourceObjectTypeDefinitionType o : objectTypesSuggestionType.getObjectType()) {
            String rawXml = "";
            try {
                rawXml = prismContext.serializerFor(PrismContext.LANG_XML).serializeRealValue(o);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize object", e);
            }

            model.addRow(new Item(o, rawXml));
        }

        JBTable table = new JBTable(model);
//        FIXME oversize content in cell of table
//        OversizeCellPreview.install(table);
        table.setRowHeight(50);

        TableColumn column = table.getColumnModel().getColumn(4);
        column.setCellRenderer(new ActionRenderer());
        column.setCellEditor(new ButtonsEditor(project, resource, this));

        detailsPanel.add(smartEditor, BorderLayout.CENTER);
        detailsPanel.setPreferredSize(new Dimension(700, 500));
        detailsPanel.setVisible(false);

        add(new JBScrollPane(table), BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(700, 350));
    }

    static class Item {
        ResourceObjectTypeDefinitionType object;
        String rawCode;
        boolean applied = false;
        boolean details = false;

        Item(ResourceObjectTypeDefinitionType object, String xml) {
            this.object = object;
            this.rawCode = xml;
        }
    }

    static class SuggestionTableModel extends AbstractTableModel {
        private final String[] columns = {"Name", "Kind", "Intent", "Description", "Activities"};
        private final List<Item> data = new ArrayList<>();

        public void addRow(Item item) {
            data.add(item);
        }

        public Item getItemAt(int row) {
            return data.get(row);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Item item = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.object.getDisplayName();
                case 1 -> item.object.getKind().value();
                case 2 -> item.object.getIntent();
                case 3 -> item.object.getDescription();
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 4 || col == 5;
        }
    }

    static class ButtonsEditor extends AbstractCellEditor implements TableCellEditor {

        private final ActionPanel panel;
        private SuggestionTableModel model;
        private int currentRow = -1;

        public ButtonsEditor(Project project, ResourceType resource, ObjectTypeSuggestionTable parent) {
            panel = new ActionPanel();

            panel.getApply().addActionListener(e -> {
                if (currentRow >= 0) {
                    Item item = model.getItemAt(currentRow);
                    if (!item.applied) {
                        PsiFile psiFile = MidPointUtils.findPsiFileByOid(project, resource.getOid());
                        if (psiFile instanceof XmlFile xmlFile) {
                            XmlTag root = xmlFile.getRootTag();

                            if (root == null || !root.getName().equals("resource")) {
                                MidPointUtils.publishNotification(
                                        project,
                                        "Apply generated suggestion",
                                        "Apply generated suggestion",
                                        "Object with oid '"  + resource.getOid() + "' is not a resource",
                                        NotificationType.ERROR
                                );
                                return;
                            }

                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                XmlElementFactory factory = XmlElementFactory.getInstance(project);
                                XmlTag schemaHandling = root.findFirstSubTag("schemaHandling");

                                if (schemaHandling == null) {
                                    schemaHandling = factory.createTagFromText("<schemaHandling/>");
                                    schemaHandling = root.addSubTag(schemaHandling, false);
                                }

                                String rawXml = item.rawCode.trim();
                                XmlTag newItemsXml = factory.createTagFromText(rawXml);
                                int startOffset = schemaHandling.getTextRange().getEndOffset();
                                XmlTag addedTag = schemaHandling.addSubTag(newItemsXml, false);
                                int endOffset = addedTag.getTextRange().getEndOffset();
                                Document doc = PsiDocumentManager.getInstance(project).getDocument(xmlFile);

                                if (doc != null) {
                                    PsiDocumentManager.getInstance(project).commitDocument(doc);
                                }

                                item.applied = false;

                                ApplicationManager.getApplication().invokeLater(() -> {
                                    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                                    if (editor == null) return;

                                    MarkupModel markup = editor.getMarkupModel();
                                    TextAttributes attrs = EditorColorsManager.getInstance()
                                            .getGlobalScheme()
                                            .getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);

                                    RangeHighlighter highlighter = markup.addRangeHighlighter(
                                            startOffset,
                                            endOffset,
                                            HighlighterLayer.SELECTION - 1,
                                            attrs,
                                            HighlighterTargetArea.EXACT_RANGE
                                    );

                                    Runnable removeHighlighter = () -> {
                                        if (highlighter.isValid()) {
                                            markup.removeHighlighter(highlighter);
                                        }
                                    };

                                    DocumentListener docListener = new DocumentListener() {
                                        @Override
                                        public void beforeDocumentChange(@NotNull DocumentEvent event) {
                                            removeHighlighter.run();
                                        }
                                    };
                                    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(docListener, project);

                                    MessageBusConnection connection = project.getMessageBus().connect();
                                    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                                        @Override
                                        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                                            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
                                            if (currentFile != null && currentFile.equals(file)) {
                                                removeHighlighter.run();
                                                connection.disconnect();
                                            }
                                        }
                                    });

                                    editor.getCaretModel().moveToOffset(startOffset);
                                    editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                                });
                            });
                        } else {
                            MidPointUtils.publishNotification(
                                    project,
                                    "Apply generated suggestion",
                                    "Apply generated suggestion",
                                    "File not found with oid '"  + resource.getOid() + "'",
                                    NotificationType.ERROR
                            );
                        }
                    }
                }

                fireEditingStopped();
            });

            panel.getDiscard().addActionListener(e -> {
                // TODO discard action impl
                fireEditingStopped();
            });

            panel.getDetails().addActionListener(e -> {
                if (currentRow < 0) return;

                Item clickedItem = model.getItemAt(currentRow);

                for (int i = 0; i < model.getRowCount(); i++) {
                    if (i != currentRow) {
                        Item it = model.getItemAt(i);
                        if (it.details) {
                            it.details = false;
                            parent.hideDetails();
                            model.fireTableRowsUpdated(i, i);
                        }
                    }
                }

                clickedItem.details = !clickedItem.details;
                model.fireTableRowsUpdated(currentRow, currentRow);

                if (clickedItem.details) {
                    parent.showDetails(clickedItem, project);
                    panel.getDetails().setText("Hide Xml");
                } else {
                    parent.hideDetails();
                    panel.getDetails().setText("Show Xml");
                }


                fireEditingStopped();
            });
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (table.getModel() instanceof SuggestionTableModel suggestionTableModel) {
                this.model = suggestionTableModel;
            }

            currentRow = row;
            return panel;
        }
    }

    public void showDetails(Item item, Project project) {
        if (item == null) return;

        WriteCommandAction.runWriteCommandAction(project, () -> {
            smartEditor.setText(item.rawCode != null ? item.rawCode : "");
        });
        smartEditor.setViewer(true);
        detailsPanel.setVisible(true);
        revalidate();
        repaint();
    }

    public void hideDetails() {
        if (!detailsPanel.isVisible()) return;
        detailsPanel.setVisible(false);
        revalidate();
        repaint();
    }
}