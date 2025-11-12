/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component;

import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardDialog;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceObjectTypeDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SchemaHandlingType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
    }

    @Override
    protected void buildSteps(ResourceDialogContext resource) {
        steps.add(createResourceSelectPanel(context.getResources(), context.uploadedResourceOid));
    }

    @Override
    protected void onFinish(ResourceDialogContext context) {

    }

    public JPanel createResourceSelectPanel(SearchResultList<ObjectType> resources, String uploadedResourceOid) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        int selectedItem = 0;
        List<Pair<ObjectType, String>> listOfResource = new ArrayList<>();

        for (int i = 0; i < resources.size(); i++) {
            ObjectType o = resources.get(i);
            listOfResource.add(new Pair<>(o, o.getName().toString()));
            if (o.getOid().equals(uploadedResourceOid)) {
                selectedItem = i;
            }
        }

        JLabel resourceFieldLabel = new JLabel("Resource object type:");
        resourceFieldLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
        panel.add(resourceFieldLabel);
        JComboBox<Pair<ObjectType, String>> resourceComboBox = getPairComboBox(
                new DefaultComboBoxModel<Pair<ObjectType, String>>(listOfResource.toArray(new Pair[0]))
        );
        resourceComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        if (uploadedResourceOid != null) {
            resourceComboBox.setSelectedIndex(selectedItem);
            resourceComboBox.setEnabled(false);
        }

        panel.add(resourceComboBox);

        if (resourceComboBox.getSelectedItem() instanceof Pair<?,?> pair) {
            if (pair.getFirst() instanceof ResourceType resource) {
                showObjectClassTable(panel, resource);
            }
        }

        // trigger action after selection change
        resourceComboBox.addActionListener(e -> {
            @SuppressWarnings("unchecked")
            Pair<ObjectType, String> selected = (Pair<ObjectType, String>) resourceComboBox.getSelectedItem();
            if (selected != null) {
                if (selected.getFirst() instanceof ResourceType resource) {
                    showObjectClassTable(panel, resource);
                }
            }
        });

        return panel;
    }

    public JPanel createObjectClassTablePanel(List<ResourceObjectTypeDefinitionType> objects) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        String[] columnNames = {"Name", "Description"};

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (objects.isEmpty()) {
            JLabel errorLabel = new JLabel("Object class missing" );
            errorLabel.setForeground(JBColor.RED);
            errorLabel.setName(OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID);
            panel.add(errorLabel);
        } else {
            objects.forEach(o -> {
                String name = o.getObjectClass() != null ? o.getObjectClass().getLocalPart() : "";
                String description = o.getDescription() != null ? o.getDescription() : "";
                model.addRow(new Object[]{name, description});
            });

            JBTable table = new JBTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSelectionAllowed(true);
            table.setColumnSelectionAllowed(false);

            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        ResourceObjectTypeDefinitionType selectedObject = objects.get(selectedRow);
                        context.setObjectClass(selectedObject.getObjectClass());
                        setOKActionEnabled(context.getResourceObjectType() != null &&
                                context.getObjectClass() != null);
                    }
                }
            });

            JLabel objClassFieldLabel = new JLabel("Object Class:");
            objClassFieldLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
            panel.add(objClassFieldLabel);

            JBScrollPane scrollPane = new JBScrollPane(table);
            scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

            panel.add(scrollPane);
        }

        panel.setName(OBJECT_CLASS_TABLE_COMPONENT_ID);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }

    private void showObjectClassTable(JPanel panel, ResourceType resource) {
        context.setResourceObjectType(resource);
        SchemaHandlingType schemaHandling = resource.getSchemaHandling();
        if (schemaHandling != null)  {
            // refresh component
            for (Component c : panel.getComponents()) {
                if (c.getName() != null && (c.getName().equals(OBJECT_CLASS_TABLE_COMPONENT_ID) ||
                        c.getName().equals(OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID))) {
                    panel.remove(c);
                }
            }

            panel.revalidate();
            panel.repaint();

            panel.add(createObjectClassTablePanel(schemaHandling.getObjectType()));
        } else {
            JLabel errorLabel = new JLabel("Schema handling not found for resource: " + resource.getName());
            errorLabel.setForeground(JBColor.RED);
            errorLabel.setName(OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID);
            // refresh component
            for (Component c : panel.getComponents()) {
                if (c.getName() != null && (c.getName().equals(OBJECT_CLASS_TABLE_COMPONENT_ID) ||
                        c.getName().equals(OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID))) {
                    panel.remove(c);
                }
            }

            panel.revalidate();
            panel.repaint();
            panel.add(errorLabel);
        }
    }

    private @NotNull ComboBox<Pair<ObjectType, String>> getPairComboBox(DefaultComboBoxModel<Pair<ObjectType, String>> model) {
        ComboBox<Pair<ObjectType, String>> resourceComboBox = new ComboBox<>(model);
        resourceComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Pair) {
                    setText(((Pair<?, ?>) value).getSecond().toString());
                }
                return this;
            }
        });
        return resourceComboBox;
    }
}
