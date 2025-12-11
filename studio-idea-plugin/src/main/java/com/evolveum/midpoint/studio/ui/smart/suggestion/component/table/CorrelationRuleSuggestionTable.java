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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CorrelationRuleSuggestionTable extends JPanel {

    private final SmartEditorComponent smartEditor;
    private final JPanel detailsPanel = new JPanel(new BorderLayout());

    public CorrelationRuleSuggestionTable(
            Project project,
            PrismContext prismContext,
            ResourceType resource,
            ResourceObjectTypeDefinitionType objectType,
            CorrelationSuggestionsType correlationSuggestionType
    ) {
        setLayout(new BorderLayout());
        SuggestionTableModel model = new SuggestionTableModel();
        this.smartEditor = new SmartEditorComponent(project, XMLLanguage.INSTANCE);

        for (CorrelationSuggestionType correlation : correlationSuggestionType.getSuggestion()) {
            for (ItemsSubCorrelatorType itemsSubCorrelatorType : correlation.getCorrelation().getCorrelators().getItems()) {
                CorrelationRuleSuggestionList.SuggestionTile tile = new CorrelationRuleSuggestionList.SuggestionTile(itemsSubCorrelatorType);
                tile.setAlignmentX(Component.LEFT_ALIGNMENT);

                String rawXml = "";
                try {
                    rawXml = prismContext.serializerFor(PrismContext.LANG_XML).serializeRealValue(itemsSubCorrelatorType);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to serialize object", e);
                }

                model.addRow(new Item(itemsSubCorrelatorType, rawXml));
            }
        }

        JBTable table = new JBTable(model);
        table.setRowHeight(30);

        TableColumn activityColumn = table.getColumnModel().getColumn(5);
        activityColumn.setCellRenderer(new ButtonRenderer());
        activityColumn.setCellEditor(new ApplyButtonEditor(new JCheckBox(), model, project, resource, objectType));

        TableColumn toggleColumn = table.getColumnModel().getColumn(6);
        toggleColumn.setCellRenderer(new ButtonRenderer());
        toggleColumn.setCellEditor(new ToggleButtonEditor(new JCheckBox(), project, model, this));

        detailsPanel.add(smartEditor, BorderLayout.CENTER);
        detailsPanel.setPreferredSize(new Dimension(700, 500));
        detailsPanel.setVisible(false);

        add(new JBScrollPane(table), BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(700, 350));
    }

    class Item {
        ItemsSubCorrelatorType object;
        String rawCode;
        boolean applied = false;
        boolean expanded = false;

        Item(ItemsSubCorrelatorType object, String xml) {
            this.object = object;
            this.rawCode = xml;
        }
    }

    class SuggestionTableModel extends AbstractTableModel {
        private final String[] columns = {"Name", "Description", "Items", "Weight", "Tier", "Efficiency", "Actions",  "Details"};
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
                case 1 -> item.object.getDescription();
                case 2 -> item.object.getItem();
                case 3 -> item.object.getComposition().getWeight();
                case 4 -> item.object.getComposition().getTier();
                case 5 -> "100";
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 5 || col == 6;
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }


    class ApplyButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int currentRow = -1;

        public ApplyButtonEditor(JCheckBox checkBox, SuggestionTableModel model, Project project, ResourceType resource, ResourceObjectTypeDefinitionType objectType) {
            super(checkBox);
            this.button = new JButton();
            this.button.setOpaque(true);

            this.button.addActionListener(e -> {
                fireEditingStopped();
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

                            XmlTag objectTypeElement = MidPointUtils.findObjectTypeById(root, objectType.getId().toString());

                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                XmlElementFactory factory = XmlElementFactory.getInstance(project);

                                assert objectTypeElement != null;

                                XmlTag correlations = objectTypeElement.findFirstSubTag("correlation");
                                if (correlations == null) {
                                    correlations = factory.createTagFromText("<correlation/>");
                                    correlations = objectTypeElement.addSubTag(correlations, false);
                                }

                                XmlTag correlation = correlations.findFirstSubTag("correlator");
                                if (correlation == null) {
                                    correlation = factory.createTagFromText("<correlator/>");
                                    correlation = correlations.addSubTag(correlation, false);
                                }

                                String rawXml = item.rawCode.trim();
                                XmlTag newItemsXml = factory.createTagFromText(rawXml);
                                int startOffset = correlation.getTextRange().getEndOffset();
                                XmlTag addedTag = correlation.addSubTag(newItemsXml, false);
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
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            button.setText(value == null ? "" : value.toString());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }

    class ToggleButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int currentRow = -1;
        private final SuggestionTableModel model;

        public ToggleButtonEditor(
                JCheckBox checkBox,
                Project project,
                SuggestionTableModel model,
                CorrelationRuleSuggestionTable parent
        ) {
            super(checkBox);
            this.model = model;

            this.button = new JButton();
            this.button.setOpaque(true);

            this.button.addActionListener(e -> {
                fireEditingStopped();

                if (currentRow < 0) return;

                Item clickedItem = model.getItemAt(currentRow);

                for (int i = 0; i < model.getRowCount(); i++) {
                    if (i != currentRow) {
                        Item it = model.getItemAt(i);
                        if (it.expanded) {
                            it.expanded = false;
                            parent.hideDetails();
                            model.fireTableRowsUpdated(i, i);
                        }
                    }
                }

                clickedItem.expanded = !clickedItem.expanded;
                model.fireTableRowsUpdated(currentRow, currentRow);

                if (clickedItem.expanded) {
                    parent.showDetails(clickedItem, project);
                } else {
                    parent.hideDetails();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column
        ) {
            currentRow = row;
            Item item = model.getItemAt(row);
            button.setText(item.expanded ? "Hide" : "Show");
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }

    public void showDetails(Item item, Project project) {
        if (item == null) return;

        WriteCommandAction.runWriteCommandAction(project, () -> {
//            detailsArea.writeText(item.rawCode != null ? item.rawCode : "");
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
