/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.resource;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.ui.editor.EditorPanel;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class ObjectTypeSuggestionTable extends JPanel {

    private final EditorPanel detailsArea;
    private final JPanel detailsPanel = new JPanel(new BorderLayout());

    public ObjectTypeSuggestionTable(Project project, PrismContext prismContext, ResourceType resource, ObjectTypesSuggestionType objectTypesSuggestionType) {
        setLayout(new BorderLayout());
        SuggestionTableModel model = new SuggestionTableModel();
        this.detailsArea = new EditorPanel(project, "", "xml");

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
        table.setRowHeight(30);

        TableColumn activityColumn = table.getColumnModel().getColumn(0);
        activityColumn.setCellRenderer(new ButtonRenderer());
        activityColumn.setCellEditor(new ApplyButtonEditor(new JCheckBox(), model, project, resource));

        TableColumn toggleColumn = table.getColumnModel().getColumn(5);
        toggleColumn.setCellRenderer(new ButtonRenderer());
        toggleColumn.setCellEditor(new ToggleButtonEditor(new JCheckBox(), project, model, this));

        detailsPanel.add(detailsArea, BorderLayout.CENTER);
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
        boolean expanded = false;

        Item(ResourceObjectTypeDefinitionType object, String xml) {
            this.object = object;
            this.rawCode = xml;
        }
    }

    static class SuggestionTableModel extends AbstractTableModel {
        private final String[] columns = {"Activity", "Name", "Kind", "Intent", "Description", "Details"};
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
                case 0 -> item.applied ? "Applied" : "Apply";
                case 1 -> item.object.getDisplayName();
                case 2 -> item.object.getKind().value();
                case 3 -> item.object.getIntent();
                case 4 -> item.object.getDescription();
                case 5 -> item.expanded ? "Hide" : "Show";
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 0 || col == 5;
        }
    }

    static class ButtonRenderer extends JButton implements TableCellRenderer {
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

    static class ApplyButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int currentRow = -1;

        public ApplyButtonEditor(JCheckBox checkBox, SuggestionTableModel model, Project project, ResourceType resource) {
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

                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                XmlElementFactory factory = XmlElementFactory.getInstance(project);
                                XmlTag schemaHandling = root.findFirstSubTag("schemaHandling");

                                if (schemaHandling == null) {
                                    schemaHandling = factory.createTagFromText("<schemaHandling/>");
                                    schemaHandling = root.addSubTag(schemaHandling, false);
                                }

                                XmlTag objectTypeTag = factory.createTagFromText(item.rawCode.trim());
                                schemaHandling.addSubTag(objectTypeTag, false);
                                Document doc = PsiDocumentManager.getInstance(project).getDocument(xmlFile);
                                if (doc != null) {
                                    PsiDocumentManager.getInstance(project).commitDocument(doc);
                                }
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

    static class ToggleButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int currentRow = -1;
        private final SuggestionTableModel model;

        public ToggleButtonEditor(
                JCheckBox checkBox,
                Project project,
                SuggestionTableModel model,
                ObjectTypeSuggestionTable parent
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
            detailsArea.setText(item.rawCode != null ? item.rawCode : "");
        });
        detailsArea.setViewer(true);
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