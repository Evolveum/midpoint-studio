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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
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
import java.awt.event.ItemEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceObjectTypeWizard extends WizardDialog<ResourceDialogContext> {

    private final String OBJECT_CLASS_TABLE_COMPONENT_ID = "object_class_table_component";
    private final String OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID = "object_class_error_label_component";
    private final String DIRECTION_COMPONENT_ID = "direction_component_id";

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
        steps.add(createResourceSelectPanel(dialogWizardContext.getResources()));
    }

    @Override
    protected void onFinish(ResourceDialogContext context) {
    }

    private JPanel createResourceSelectPanel(SearchResultList<ObjectType> resources) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        int defaultSelectedRow = -1;
        int rowCount = resources != null ? resources.size() : 0;

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
            if (obj.getOid().equals(dialogWizardContext.getResourceOid())) {
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
        scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth(), 300));
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
                .filter(r -> dialogWizardContext.getResourceOid().equals(r.getOid()))
                .findFirst()
                .orElse(null);

            if (dialogWizardContext.getMode().equals(ResourceDialogContext.ResourceDialogContextMode.OBJECT_TYPE)) {
                displayObjectClassTable(panel, resource);
            } else {
                displayObjectTypeTable(panel, resource);
            }
        } else {
            resourceTable.getSelectionModel().addListSelectionListener(resourceEvent -> {
                if (!resourceEvent.getValueIsAdjusting()) {
                    int selectedRow = resourceTable.getSelectedRow();

                    if (selectedRow >= 0) {
                        String resourceOid = (String) resourceTable.getValueAt(selectedRow, 0);
                        dialogWizardContext.setResourceOid(resourceOid);
                        dialogWizardContext.setObjectType(null);
                        dialogWizardContext.setObjectClass(null);
                        setOKActionEnabled(false);

                        ResourceType resource = resources.stream()
                                .filter(o -> o instanceof ResourceType)
                                .map(o -> (ResourceType) o)
                                .filter(r -> resourceOid.equals(r.getOid()))
                                .findFirst()
                                .orElse(null);

                        if (dialogWizardContext.getMode().equals(ResourceDialogContext.ResourceDialogContextMode.OBJECT_TYPE)) {
                            displayObjectClassTable(panel, resource);
                        } else {
                            displayObjectTypeTable(panel, resource);
                        }
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

    private void displayObjectTypeTable(JPanel panel, ResourceType resource) {
        removeComponent(panel, OBJECT_CLASS_TABLE_COMPONENT_ID);
        removeComponent(panel, OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID);

        if (resource == null) {
            return;
        }

        SchemaHandlingType schemaHandling = resource.getSchemaHandling();

        if (schemaHandling != null) {
            var objectTypeList = schemaHandling.getObjectType();
            String[] objTypeNameColumns = new String[]{"Value", "Name", "Description"};
            Object[][] objTypeResourceRows = new Object[15][objTypeNameColumns.length];

            DefaultTableModel objTypeTableModel = new DefaultTableModel(objTypeResourceRows, objTypeNameColumns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            int i = 0;
            for (ResourceObjectTypeDefinitionType objectType : objectTypeList) {
                objTypeTableModel.setValueAt(objectType, i, 0);
                objTypeTableModel.setValueAt(objectType.getDisplayName(), i, 1);
                objTypeTableModel.setValueAt(objectType.getDescription(), i, 2);
                i++;
            }

            JBTable objTypeTable = createTableComponent(objTypeTableModel);
            hideColumn(objTypeTable, 0);
            JBScrollPane objTypeScrollPane = new JBScrollPane(objTypeTable);
            objTypeScrollPane.setPreferredSize(new Dimension(objTypeScrollPane.getWidth(), 200));
            objTypeScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
            TitledBorder titleBorder = BorderFactory.createTitledBorder("Object type:");
            titleBorder.setTitleFont(titleBorder.getTitleFont().deriveFont(Font.BOLD));
            titleBorder.setTitleFont(titleBorder.getTitleFont().deriveFont(JBUI.scale(15f)));
            Border spaceBorder = BorderFactory.createEmptyBorder(0, 0, 25, 0);
            objTypeScrollPane.setBorder(BorderFactory.createCompoundBorder(spaceBorder, titleBorder));
            objTypeScrollPane.setName(OBJECT_CLASS_TABLE_COMPONENT_ID);
            panel.add(objTypeScrollPane);

            objTypeTable.getSelectionModel().addListSelectionListener(objTypeEvent -> {
                int objTypeSelectedRow = objTypeTable.getSelectedRow();
                if (!objTypeEvent.getValueIsAdjusting() && objTypeSelectedRow >= 0) {
                    dialogWizardContext.setObjectType((ResourceObjectTypeDefinitionType) objTypeTable.getValueAt(objTypeSelectedRow, 0));
                    setOKActionEnabled(dialogWizardContext.getResourceOid() != null && dialogWizardContext.getObjectType() != null);
                }
            });
        } else {
            printErrorMsg(panel, "Not found schemaHandling in resource '" + resource.getOid() + "'");
        }

        removeComponent(panel, DIRECTION_COMPONENT_ID);

        if (dialogWizardContext.getMode().equals(ResourceDialogContext.ResourceDialogContextMode.MAPPING)) {
            String[] names = {"Inbound", "Outbound"};
            ResourceDialogContext.Direction[] values = {ResourceDialogContext.Direction.INBOUND, ResourceDialogContext.Direction.OUTBOUND};

            LabeledComponent<JComboBox<String>> dropdown =
                    createDropdown("Inbound/Outbound mapping", names, values);
            dropdown.setName(DIRECTION_COMPONENT_ID);
            dropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, dropdown.getHeight()));
            dialogWizardContext.setDirection(getSelectedValue(dropdown));

            panel.add(dropdown);
        }
    }

    private void displayObjectClassTable(JPanel panel, ResourceType resource) {
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

                DefaultTableModel objClassTableModel = new DefaultTableModel(objClassResourceRows, objClassNameColumns) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                int i = 0;
                for (ResourceObjectClassDefinition def : definitions) {
                    objClassTableModel.setValueAt(def.getObjectClassName(), i, 0);
                    objClassTableModel.setValueAt(def.getObjectClassName().getLocalPart(), i, 1);
                    objClassTableModel.setValueAt(def.getDescription(), i, 2);
                    i++;
                }

                JBTable objectClassTable = createTableComponent(objClassTableModel);
                hideColumn(objectClassTable, 0);
                JBScrollPane objectClassScrollPane = new JBScrollPane(objectClassTable);
                objectClassScrollPane.setPreferredSize(new Dimension(objectClassScrollPane.getWidth(), 200));
                objectClassScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
                TitledBorder titleBorder = BorderFactory.createTitledBorder("Object class:");
                titleBorder.setTitleFont(titleBorder.getTitleFont().deriveFont(Font.BOLD));
                titleBorder.setTitleFont(titleBorder.getTitleFont().deriveFont(JBUI.scale(15f)));
                Border spaceBorder = BorderFactory.createEmptyBorder(0, 0, 25, 0);
                objectClassScrollPane.setBorder(BorderFactory.createCompoundBorder(spaceBorder, titleBorder));
                objectClassScrollPane.setName(OBJECT_CLASS_TABLE_COMPONENT_ID);
                panel.add(objectClassScrollPane);

                objectClassTable.getSelectionModel().addListSelectionListener(objectClassEvent -> {
                    int objectClassSelectedRow = objectClassTable.getSelectedRow();
                    if (!objectClassEvent.getValueIsAdjusting() && objectClassSelectedRow >= 0) {
                        var objectClassValue = objectClassTable.getValueAt(objectClassSelectedRow, 0);

                        if (objectClassValue != null) {
                            dialogWizardContext.setObjectClass(QName.valueOf(objectClassValue.toString()));
                        }

                        setOKActionEnabled(dialogWizardContext.getResourceOid() != null && dialogWizardContext.getObjectClass() != null);
                    }
                });
            } else {
                printErrorMsg(panel, "Not found schema in resource '" + resource.getOid() + "'");
            }
        } catch (SchemaException | ConfigurationException ex) {
            printErrorMsg(panel, ex.getMessage());
        }
    }

    private LabeledComponent<JComboBox<String>> createDropdown(String label, String[] names, ResourceDialogContext.Direction[] values) {
        if (names.length != values.length) {
            throw new IllegalArgumentException("Names and values must have the same length");
        }

        Map<String, ResourceDialogContext.Direction> valueMap = new LinkedHashMap<>();
        for (int i = 0; i < names.length; i++) {
            valueMap.put(names[i], values[i]);
        }

        JComboBox<String> comboBox = new ComboBox<>(names);
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboBox.putClientProperty("valueMap", valueMap);

        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                dialogWizardContext.setDirection(valueMap.get(e.getItem()));
            }
        });

        return LabeledComponent.create(comboBox, label);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSelectedValue(LabeledComponent<JComboBox<String>> dropdown) {
        JComboBox<String> combo = dropdown.getComponent();
        String key = (String) combo.getSelectedItem();

        Map<String, T> valueMap =
                (Map<String, T>) combo.getClientProperty("valueMap");

        return valueMap.get(key);
    }

    private JBTable createTableComponent(DefaultTableModel model) {
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
            if (c.getName() != null && (c.getName().equals(id))) {
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
