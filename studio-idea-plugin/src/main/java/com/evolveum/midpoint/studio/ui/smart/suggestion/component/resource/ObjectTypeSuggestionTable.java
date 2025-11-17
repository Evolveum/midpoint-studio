/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.resource;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.SerializationOptions;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectTypesSuggestionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceObjectTypeDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaHandlingType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.Gray;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class ObjectTypeSuggestionTable extends JPanel {

    private final JTextArea detailsArea = new JTextArea();
    private final JPanel detailsPanel = new JPanel(new BorderLayout());

    public ObjectTypeSuggestionTable(Project project, PrismContext prismContext, ResourceType resource, ObjectTypesSuggestionType objectTypesSuggestionType) {
        setLayout(new BorderLayout());
        SuggestionTableModel model = new SuggestionTableModel();

        for (ResourceObjectTypeDefinitionType o : objectTypesSuggestionType.getObjectType()) {
            model.addRow(new Item(o, ""));
        }

        JBTable table = new JBTable(model);
        table.setRowHeight(30);

        TableColumn activityColumn = table.getColumnModel().getColumn(0);
        activityColumn.setCellRenderer(new ButtonRenderer());
        activityColumn.setCellEditor(new ApplyButtonEditor(new JCheckBox(), model, project, prismContext, resource));

        TableColumn toggleColumn = table.getColumnModel().getColumn(5);
        toggleColumn.setCellRenderer(new ButtonRenderer());
        toggleColumn.setCellEditor(new ToggleButtonEditor(new JCheckBox(), model, this));

        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setBackground(Gray._250);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailsArea.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        detailsPanel.add(new JBScrollPane(detailsArea), BorderLayout.CENTER);
        detailsPanel.setVisible(false);

        add(new JBScrollPane(table), BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(700, 350));
    }

    static class Item {
        ResourceObjectTypeDefinitionType object;
        String xml;
        boolean applied = false;
        boolean expanded = false;

        Item(ResourceObjectTypeDefinitionType object, String xml) {
            this.object = object;
            this.xml = xml;
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
            return col == 0 || col == 1;
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

        public ApplyButtonEditor(JCheckBox checkBox, SuggestionTableModel model, Project project, PrismContext prismContext, ResourceType resource) {
            super(checkBox);
            this.button = new JButton();
            this.button.setOpaque(true);

            this.button.addActionListener(e -> {
                fireEditingStopped();
                if (currentRow >= 0) {
                    Item item = model.getItemAt(currentRow);
                    if (!item.applied) {
                        SchemaHandlingType schemaHandlingType = resource.getSchemaHandling();

                        if (schemaHandlingType == null) {
                            schemaHandlingType = new SchemaHandlingType();
                        }


                        try {
                            schemaHandlingType.getObjectType().add(item.object);
                        } catch (Exception ex) {
                            System.out.println("EXCEPTION MSG: " + ex.getMessage());
                        }

                        resource.setSchemaHandling(schemaHandlingType);



//                        try {
//                            String xml = prismContext
//                                    .xmlSerializer()
//                                    .options(SerializationOptions.createSerializeReferenceNames())
//                                    .serialize(resource.asPrismObject());
//
//                            System.out.println("KAKSAKSK>>> " + xml);
//                        } catch (SchemaException ex) {
//                            System.out.println("AKSKAKKS::: " + ex.getMessage());
//                            throw new RuntimeException(ex);
//                        }

//                        try {
//                            SchemaHandlingType schemaHandlingType = resource.getSchemaHandling();
//                            schemaHandlingType.getObjectType().add(item.object);
//                            resource.setSchemaHandling(schemaHandlingType);
//
//                            String xml = prismContext.xmlSerializer()
//                                    .serialize(resource.asPrismObject());
//
//                            FileEditorManager editorManager = FileEditorManager.getInstance(project);
//                            VirtualFile[] openFiles = editorManager.getSelectedFiles();
//
//                            if (openFiles.length > 0) {
//                                VirtualFile currentOpenFile = openFiles[0];
//                                if (currentOpenFile.getName().contains(resource.getName().getNorm())) {
//                                    WriteCommandAction.runWriteCommandAction(project, () -> {
//                                        try {
//                                            currentOpenFile.setBinaryContent(xml.getBytes(StandardCharsets.UTF_8));
//                                        } catch (Exception ex) {
//                                            ex.printStackTrace();
//                                        }
//                                    });
//                                }
//                            }
//                        } catch (SchemaException ex) {
//                            throw new RuntimeException(ex);
//                        }

                        item.applied = true;
                        model.fireTableRowsUpdated(currentRow, currentRow);
                        JOptionPane.showMessageDialog(button,
                                "Applied suggestion '" + item.object.getDisplayName() + "'");
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

        public ToggleButtonEditor(JCheckBox checkBox, SuggestionTableModel model, ObjectTypeSuggestionTable parent) {
            super(checkBox);
            this.model = model;
            this.button = new JButton();
            this.button.setOpaque(true);

            this.button.addActionListener(e -> {
                fireEditingStopped();

                if (currentRow < 0) return;

                Item item = model.getItemAt(currentRow);
                item.expanded = !item.expanded;

                model.fireTableRowsUpdated(currentRow, currentRow);

                if (item.expanded) {
                    parent.showDetails(item);
                } else {
                    parent.hideDetails();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
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

    public void showDetails(Item item) {
        if (item == null) return;
        detailsArea.setText(item.xml != null ? item.xml : "");
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