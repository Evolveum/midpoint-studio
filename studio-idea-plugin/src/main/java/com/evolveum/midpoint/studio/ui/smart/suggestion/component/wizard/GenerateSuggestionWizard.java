/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard;

import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.processor.ResourceObjectClassDefinition;
import com.evolveum.midpoint.schema.processor.ResourceObjectClassDefinitionImpl;
import com.evolveum.midpoint.schema.processor.ResourceSchema;
import com.evolveum.midpoint.schema.processor.ResourceSchemaFactory;
import com.evolveum.midpoint.studio.ui.dialog.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.dialog.wizard.MidpointWizardDialog;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.table.model.DialogWizardTableModel;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.evolveum.midpoint.studio.ui.treetable.FilterableColumnInfo;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.JBUI;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class GenerateSuggestionWizard extends MidpointWizardDialog<GenerateSuggestionDialogContext> {

    private final String OBJECT_CLASS_TABLE_COMPONENT_ID = "OBJECT_CLASS_TABLE_COMPONENT";
    private final String OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID = "OBJECT_CLASS_ERROR_LABEL_COMPONENT";
    private final String DIRECTION_COMPONENT_ID = "DIRECTION_COMPONENT";

    public GenerateSuggestionWizard(
            Project project,
            String title,
            GenerateSuggestionDialogContext context,
            DialogWindowActionHandler actionHandler
    ) {
        super(project, title, context, actionHandler, false);
        setSize(800, 600);
    }

    @Override
    protected void buildSteps(GenerateSuggestionDialogContext resource) {
//        steps.add(new MidpointWizardStep(null, createResourceTable(dialogWizardContext.getResources())));
    }

    @Override
    protected void onFinish(GenerateSuggestionDialogContext context) {
        // TODO finished implementation
    }

    private JPanel createResourceTable(SearchResultList<ObjectType> resources) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        if (resources != null && !resources.isEmpty()) {
            DialogWizardTableModel<ObjectType> model = new DialogWizardTableModel<>(List.of(
                    new FilterableColumnInfo<>("Name", obj -> {
                        if (obj instanceof ResourceType resourceType) {
                            return resourceType.getName().getOrig();
                        }

                        return null;
                    }, true),
                    new FilterableColumnInfo<>("Description", obj -> {
                        if (obj instanceof ResourceType resourceType) {
                            return resourceType.getDescription();
                        }

                        return null;
                    }, false)
            ));

            var table = createTableComponent(model);
            model.setData(resources.getList());

            TitledBorder resourceTitleBorder = BorderFactory.createTitledBorder("Resource:");
            resourceTitleBorder.setTitleFont(resourceTitleBorder.getTitleFont().deriveFont(Font.BOLD));
            resourceTitleBorder.setTitleFont(resourceTitleBorder.getTitleFont().deriveFont(JBUI.scale(15f)));

            JBScrollPane scrollPane = new JBScrollPane(table);
            scrollPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(0, 0, 25, 0),
                    resourceTitleBorder
            ));

            panel.add(scrollPane);

            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        TreePath path = table.getTree().getPathForRow(row);
                        if (path != null) {
                            if (path.getLastPathComponent() instanceof DefaultMutableTreeTableNode node) {
                                if (node.getUserObject() instanceof ResourceType resource) {
                                    dialogWizardContext.setResourceOid(resource.getOid());
                                    dialogWizardContext.setObjectType(null);
                                    dialogWizardContext.setObjectClass(null);

                                    setOKActionEnabled(false);

                                    if (dialogWizardContext.getMode().equals(GenerateSuggestionDialogContext.ResourceDialogContextMode.OBJECT_TYPE)) {
                                        displayObjectClassTable(panel, resource);
                                    } else {
                                        displayObjectTypeTable(panel, resource);
                                    }
                                }
                            }
                        }
                    }
                }
            });

            int initRow = IntStream.range(0, resources.size())
                    .filter(i -> Objects.equals(
                            resources.get(i).getOid(),
                            dialogWizardContext.getResourceOid()
                    ))
                    .findFirst()
                    .orElse(-1);

            if (initRow >= 0 && initRow < table.getRowCount()) {
                table.setRowSelectionInterval(initRow, initRow);

                if (dialogWizardContext.getMode().equals(GenerateSuggestionDialogContext.ResourceDialogContextMode.OBJECT_TYPE)) {
                    displayObjectClassTable(panel, (ResourceType) resources.get(initRow));
                } else {
                    displayObjectTypeTable(panel, (ResourceType) resources.get(initRow));
                }
            }
        } else {
            printErrorMsg(panel, "No resources found");
        }

        return panel;
    }

    private void displayObjectClassTable(JPanel panel, ResourceType resource) {
        removeComponent(panel, OBJECT_CLASS_TABLE_COMPONENT_ID);
        removeComponent(panel, OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID);

        if (resource == null) {
            printErrorMsg(panel, "Not found resource");
            return;
        }

        try {
            ResourceSchema resourceSchema = ResourceSchemaFactory.parseCompleteSchema(resource);
            if (resourceSchema != null) {
                var definitions = resourceSchema.getObjectClassDefinitions();
                DialogWizardTableModel<ResourceObjectClassDefinition> model = new DialogWizardTableModel<>(List.of(
                        new FilterableColumnInfo<>("Name", obj -> {
                            if (obj instanceof ResourceObjectClassDefinitionImpl objectTypeDefinitionType) {
                                return objectTypeDefinitionType.getObjectClassName().getLocalPart();
                            }

                            return null;
                        }, true),
                        new FilterableColumnInfo<>("Description", obj -> {
                            if (obj instanceof ResourceObjectClassDefinitionImpl objectTypeDefinitionType) {
                                return objectTypeDefinitionType.getDescription();
                            }

                            return null;
                        }, true)
                ));

                model.setData(definitions.stream().toList());
                var table = createTableComponent(model);
                table.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int row = table.rowAtPoint(e.getPoint());
                        if (row >= 0) {
                            TreePath path = table.getTree().getPathForRow(row);
                            if (path.getLastPathComponent() instanceof DefaultMutableTreeTableNode node) {
                                if (node.getUserObject() instanceof ResourceObjectClassDefinition objectClass) {
                                    dialogWizardContext.setObjectClass(objectClass);
                                    setOKActionEnabled(dialogWizardContext.getResourceOid() != null && dialogWizardContext.getObjectClass() != null);
                                }
                            }
                        }
                    }
                });

                JBScrollPane objectClassScrollPane = new JBScrollPane(table);
                objectClassScrollPane.setPreferredSize(new Dimension(objectClassScrollPane.getWidth(), 200));
                objectClassScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

                TitledBorder titleBorder = BorderFactory.createTitledBorder("Object class:");
                titleBorder.setTitleFont(titleBorder.getTitleFont().deriveFont(Font.BOLD));
                titleBorder.setTitleFont(titleBorder.getTitleFont().deriveFont(JBUI.scale(15f)));

                Border spaceBorder = BorderFactory.createEmptyBorder(0, 0, 25, 0);
                objectClassScrollPane.setBorder(BorderFactory.createCompoundBorder(spaceBorder, titleBorder));
                objectClassScrollPane.setName(OBJECT_CLASS_TABLE_COMPONENT_ID);
                panel.add(objectClassScrollPane);
            } else {
                printErrorMsg(panel, "Not found schema in resource '" + resource.getOid() + "'");
            }
        } catch (SchemaException | ConfigurationException ex) {
            printErrorMsg(panel, ex.getMessage());
        }
    }

    private void displayObjectTypeTable(JPanel panel, ResourceType resource) {
        removeComponent(panel, OBJECT_CLASS_TABLE_COMPONENT_ID);
        removeComponent(panel, OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID);

        if (resource == null) {
            printErrorMsg(panel, "Not found resource");
            return;
        }

        SchemaHandlingType schemaHandling = resource.getSchemaHandling();

        if (schemaHandling != null) {
            DialogWizardTableModel<ResourceObjectTypeDefinitionType> model = new DialogWizardTableModel<>(List.of(
                    new FilterableColumnInfo<>("Name", obj -> {
                        if (obj instanceof ResourceObjectTypeDefinitionType objectTypeDefinitionType) {
                            return objectTypeDefinitionType.getDisplayName();
                        }

                        return null;
                    }, true),
                    new FilterableColumnInfo<>("Description", obj -> {
                        if (obj instanceof ResourceObjectTypeDefinitionType objectTypeDefinitionType) {
                            return objectTypeDefinitionType.getDescription();
                        }

                        return null;
                    },true)
            ));

            model.setData(schemaHandling.getObjectType());

            var table = createTableComponent(model);
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        TreePath path = table.getTree().getPathForRow(row);
                        if (path.getLastPathComponent() instanceof DefaultMutableTreeTableNode node) {
                            if (node.getUserObject() instanceof ResourceObjectTypeDefinitionType objectTypeDefinition) {
                                dialogWizardContext.setObjectType(objectTypeDefinition);
                                setOKActionEnabled(dialogWizardContext.getResourceOid() != null && dialogWizardContext.getObjectType() != null);
                            }
                        }
                    }
                }
            });

            JBScrollPane objTypeScrollPane = new JBScrollPane(table);
            objTypeScrollPane.setPreferredSize(new Dimension(objTypeScrollPane.getWidth(), 200));
            objTypeScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            TitledBorder titleBorder = BorderFactory.createTitledBorder("Object type:");
            titleBorder.setTitleFont(titleBorder.getTitleFont().deriveFont(Font.BOLD));
            titleBorder.setTitleFont(titleBorder.getTitleFont().deriveFont(JBUI.scale(15f)));

            Border spaceBorder = BorderFactory.createEmptyBorder(0, 0, 25, 0);
            objTypeScrollPane.setBorder(BorderFactory.createCompoundBorder(spaceBorder, titleBorder));
            objTypeScrollPane.setName(OBJECT_CLASS_TABLE_COMPONENT_ID);
            panel.add(objTypeScrollPane);
        } else {
            printErrorMsg(panel, "Not found schemaHandling in resource '" + resource.getOid() + "'");
        }

        removeComponent(panel, DIRECTION_COMPONENT_ID);

        if (dialogWizardContext.getMode().equals(GenerateSuggestionDialogContext.ResourceDialogContextMode.MAPPING)) {
            String[] names = {"Inbound", "Outbound"};
            GenerateSuggestionDialogContext.Direction[] values = {GenerateSuggestionDialogContext.Direction.INBOUND, GenerateSuggestionDialogContext.Direction.OUTBOUND};

            LabeledComponent<JComboBox<String>> dropdown = createDropdown(names, values);
            dropdown.setName(DIRECTION_COMPONENT_ID);
            dropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, dropdown.getHeight()));
            dialogWizardContext.setDirection(getSelectedValue(dropdown));

            panel.add(dropdown);
        }
    }

    private LabeledComponent<JComboBox<String>> createDropdown(String[] names, GenerateSuggestionDialogContext.Direction[] values) {
        if (names.length != values.length) {
            throw new IllegalArgumentException("Names and values must have the same length");
        }

        Map<String, GenerateSuggestionDialogContext.Direction> valueMap = new LinkedHashMap<>();
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

        return LabeledComponent.create(comboBox, "Inbound/Outbound mapping");
    }

    @SuppressWarnings("unchecked")
    public <T> T getSelectedValue(LabeledComponent<JComboBox<String>> dropdown) {
        JComboBox<String> combo = dropdown.getComponent();
        String key = (String) combo.getSelectedItem();
        Map<String, T> valueMap = (Map<String, T>) combo.getClientProperty("valueMap");

        return valueMap.get(key);
    }

    private TreeTable createTableComponent(DialogWizardTableModel<?> model) {
        var table = new DefaultTreeTable<>(model);
        table.setShowColumns(true);
        table.setRootVisible(false);
        table.setDragEnabled(false);
        table.setRowHeight(30);

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

    private void printErrorMsg(JPanel panel, String msg) {
        JLabel errorLabel = new JLabel(msg);
        errorLabel.setForeground(JBColor.RED);
        errorLabel.setName(OBJECT_CLASS_ERROR_LABEL_COMPONENT_ID);
        panel.add(errorLabel);
    }
}
