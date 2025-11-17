/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component;

import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.processor.ResourceObjectClassDefinition;
import com.evolveum.midpoint.schema.processor.ResourceSchema;
import com.evolveum.midpoint.schema.processor.ResourceSchemaFactory;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardDialog;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.xml.namespace.QName;
import java.awt.*;

public class ResourceObjectTypeWizard extends WizardDialog<ResourceDialogContext> {

    private final String OBJECT_CLASS_TABLE_COMPONENT_ID = "object_class_table_component";
    private final String OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID = "object_class_error_label_component";

    public ResourceObjectTypeWizard(
            Project project,
            String title,
            ResourceDialogContext context,
            DialogWindowActionHandler actionHandler
    ) {
        super(project, title, context, actionHandler);
        setSize(800, 600);
    }

    @Override
    protected void buildSteps(ResourceDialogContext resource) {
        steps.add(createResourceSelectPanel(context.getResources(), context.resourceOid));
    }

    @Override
    protected void onFinish(ResourceDialogContext context) {

    }

    public JPanel createResourceSelectPanel(SearchResultList<ObjectType> resources, String uploadedResourceOid) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        int defaultSelectedRow = -1;
        int rowCount = resources.size();
        String[] nameColumns = new String[]{"Value", "Name", "Display name", "Description"};
        Object[][] resourceRows = new Object[rowCount][nameColumns.length];

        DefaultTableModel resourceTableModel = new DefaultTableModel(resourceRows, nameColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        int i = 0;
        for (ObjectType obj : resources) {
            if (obj.getOid().equals(context.getResourceOid())) {
                defaultSelectedRow = i;
            }

            resourceTableModel.setValueAt(obj.getOid(), i, 0);
            resourceTableModel.setValueAt(obj.getName().getOrig(), i, 1);
            resourceTableModel.setValueAt(obj.asPrismContainer().getDisplayName(), i, 2);
            resourceTableModel.setValueAt(obj.getDescription(), i, 3);
            i++;
        }

        JBTable resourceTable = createTableComponent(resourceTableModel);
        hideColumn(resourceTable, 0);
        JBScrollPane scrollPane = new JBScrollPane(resourceTable);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        TitledBorder resourceTitleBorder = BorderFactory.createTitledBorder("Resource object type:");
        resourceTitleBorder.setTitleFont(resourceTitleBorder.getTitleFont().deriveFont(Font.BOLD));
        resourceTitleBorder.setTitleFont(resourceTitleBorder.getTitleFont().deriveFont(JBUI.scale(15f)));
        Border resourceSpaceBorder = BorderFactory.createEmptyBorder(0, 0, 25, 0);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(resourceSpaceBorder, resourceTitleBorder));

        panel.add(scrollPane);

        if (defaultSelectedRow > -1) {
            DefaultListSelectionModel model = getDefaultListSelectionModel(defaultSelectedRow);
            resourceTable.setSelectionModel(model);
            resourceTable.setRowSelectionInterval(defaultSelectedRow, defaultSelectedRow);

            ResourceType resource = resources.stream()
                .filter(o -> o instanceof ResourceType)
                .map(o -> (ResourceType) o)
                .filter(r -> context.getResourceOid().equals(r.getOid()))
                .findFirst()
                .orElse(null);

            displayObjectClassTable(panel, resource);
        } else {
            resourceTable.getSelectionModel().addListSelectionListener(resourceEvent -> {
                if (!resourceEvent.getValueIsAdjusting()) {
                    int selectedRow = resourceTable.getSelectedRow();

                    if (selectedRow >= 0) {
                        String resourceOid = (String) resourceTable.getValueAt(selectedRow, 0);
                        context.setResourceOid(resourceOid);

                        ResourceType resource = resources.stream()
                                .filter(o -> o instanceof ResourceType)
                                .map(o -> (ResourceType) o)
                                .filter(r -> resourceOid.equals(r.getOid()))
                                .findFirst()
                                .orElse(null);

                        displayObjectClassTable(panel, resource);
                    }
                }
            });
        }

        return panel;
    }

    private @NotNull DefaultListSelectionModel getDefaultListSelectionModel(int defaultSelectedRow) {
        return new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                super.setSelectionInterval(defaultSelectedRow, defaultSelectedRow);
            }

            @Override
            public void addSelectionInterval(int index0, int index1) {
                super.setSelectionInterval(defaultSelectedRow, defaultSelectedRow);
            }
        };
    }

    public void displayObjectClassTable(JPanel panel, ResourceType resource) {
        removeComponent(panel, OBJECT_CLASS_TABLE_COMPONENT_ID);
        removeComponent(panel, OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID);

        try {
            assert resource != null;
            ResourceSchema resourceSchema = ResourceSchemaFactory.parseCompleteSchema(resource);
            // create object class table
            if (resourceSchema != null) {
                var definitions = resourceSchema.getObjectClassDefinitions();
                String[] objClassNameColumns = new String[]{"Value", "Name", "Description"};
                Object[][] objClassResourceRows = new Object[definitions.size()][objClassNameColumns.length];
                int ii = 0;

                DefaultTableModel objClassTableModel = new DefaultTableModel(objClassResourceRows, objClassNameColumns) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                for (ResourceObjectClassDefinition def : definitions) {
                    objClassTableModel.setValueAt(def.getObjectClassName(), ii, 0);
                    objClassTableModel.setValueAt(def.getObjectClassName().getLocalPart(), ii, 1);
                    objClassTableModel.setValueAt(def.getDescription(), ii, 2);
                    ii++;
                }

                JBTable objectClassTable = createTableComponent(objClassTableModel);
                hideColumn(objectClassTable, 0);

                objectClassTable.getSelectionModel().addListSelectionListener(objectClassEvent -> {
                    int objectClassSelectedRow = objectClassTable.getSelectedRow();
                    if (!objectClassEvent.getValueIsAdjusting() && objectClassSelectedRow >= 0) {
                        var objectClassValue = objectClassTable.getValueAt(objectClassSelectedRow, 0);
                        context.setObjectClass(QName.valueOf(objectClassValue.toString()));
                        setOKActionEnabled(context.getResourceOid() != null && context.getObjectClass() != null);
                    }
                });

                JPanel objectClassTablePanel = ToolbarDecorator.createDecorator(objectClassTable).createPanel();
                TitledBorder objectClassTitleBorder = BorderFactory.createTitledBorder("Object class:");
                objectClassTitleBorder.setTitleFont(objectClassTitleBorder.getTitleFont().deriveFont(Font.BOLD));
                objectClassTitleBorder.setTitleFont(objectClassTitleBorder.getTitleFont().deriveFont(JBUI.scale(15f)));
                objectClassTablePanel.setBorder(objectClassTitleBorder);
                objectClassTablePanel.setName(OBJECT_CLASS_TABLE_COMPONENT_ID);
                panel.add(objectClassTablePanel);
            } else {
                printErrorMsg(panel, "Not found schema in resource '" + resource.getOid() + "'");
            }
        } catch (SchemaException | ConfigurationException ex) {
            printErrorMsg(panel, ex.getMessage());
        }
    }

    public JBTable createTableComponent(DefaultTableModel model) {
        JBTable table = new JBTable(model);
        table.setStriped(true);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);

        return table;
    }

    private void removeComponent(JPanel panel, String id) {
        for (Component c : panel.getComponents()) {
            if (c.getName() != null && (c.getName().equals(id) ||
                    c.getName().equals(id))) {
                panel.remove(c);
            }
        }

        panel.revalidate();
        panel.repaint();
    }

    private void hideColumn(JBTable table, int idColumnIndex) {
        table.getColumnModel().getColumn(idColumnIndex).setMinWidth(0);
        table.getColumnModel().getColumn(idColumnIndex).setMaxWidth(0);
        table.getColumnModel().getColumn(idColumnIndex).setPreferredWidth(0);
        table.getColumnModel().getColumn(idColumnIndex).setWidth(0);
    }

    private void printErrorMsg(JPanel panel, String msg) {
        JLabel errorLabel = new JLabel(msg);
        errorLabel.setForeground(JBColor.RED);
        errorLabel.setName(OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID);
        panel.add(errorLabel);
    }
}
