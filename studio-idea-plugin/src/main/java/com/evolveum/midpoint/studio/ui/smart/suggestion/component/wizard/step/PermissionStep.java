/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.step;

import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionDataModel;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionWizard;
import com.evolveum.midpoint.xml.ns._public.common.common_3.DataAccessPermissionType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jspecify.annotations.NonNull;
import com.intellij.openapi.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PermissionStep extends StepAdapter {

    private final GenerateSuggestionWizard wizard;
    private final GenerateSuggestionDataModel dataModel;

    public enum DataAccessPermission {

        STATISTICS_ACCESS(new Pair<>(
                DataAccessPermissionType.STATISTICS_ACCESS,
                "Allows reading data from the application."
        )),

        SCHEMA_ACCESS(new Pair<>(
                DataAccessPermissionType.SCHEMA_ACCESS,
                "Allows modifying data in the application."
        )),

        RAW_DATA_ACCESS(new Pair<>(
                 DataAccessPermissionType.RAW_DATA_ACCESS,
                "Allows modifying data in the application."
        ));

        private final Pair<DataAccessPermissionType, String> item;

        DataAccessPermission(Pair<DataAccessPermissionType, String> item) {
            this.item = item;
        }

        public DataAccessPermissionType getType() {
            return item.first;
        }

        public String getDescription() {
            return item.second;
        }
    }

    public PermissionStep(
            GenerateSuggestionWizard wizard,
            GenerateSuggestionDataModel dataModel
    ) {
        this.wizard = wizard;
        this.dataModel = dataModel;
    }

    @Override
    public JComponent getComponent() {
        return createPermissionPanel();
    }

    private JPanel createPermissionPanel() {
        return createContentPanel();
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JBLabel title = new JBLabel("Allow AI to analyze your resource data?");
        title.setIcon(AllIcons.General.Information);
        content.add(title);

        JBLabel description = new JBLabel(
                "<html>" +
                        "To generate accurate suggestions, selected resource data " +
                        "may be sent to an external AI service for processing." +
                        "</html>"
        );

//        description.setBorder(JBUI.Borders.empty(20, 0));
        content.add(description);

        JBLabel credentialsInfo = new JBLabel(
                "<html>" +
                        "No authentication credentials or user passwords will be shared." +
                        "</html>"
        );

//        credentialsInfo.setBorder(JBUI.Borders.emptyBottom(20));
        content.add(credentialsInfo);

        JBLabel limitationInfo = new JBLabel(
                "<html>" +
                        "Without the selected data, suggestions may be limited to " +
                        "built-in heuristics or be unavailable." +
                        "</html>"
        );

//        limitationInfo.setBorder(JBUI.Borders.emptyBottom(20));
        content.add(limitationInfo);

        JPanel resourcePanel = createResourcePanel();
        content.add(resourcePanel);

//        content.add(Box.createVerticalStrut(20));

        JBLabel optionsTitle = new JBLabel("Select which data can be used:");

        optionsTitle.setFont(
                UIUtil.getLabelFont().deriveFont(Font.BOLD)
        );

        content.add(optionsTitle);
        content.add(Box.createVerticalStrut(8));

        JPanel checkboxPanel = createCheckboxPanel();
        content.add(checkboxPanel);

        return content;
    }


    private @NonNull JPanel createCheckboxPanel() {
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));

        for (DataAccessPermission permission : DataAccessPermission.values()) {
            JCheckBox checkbox = new JCheckBox();

            checkbox.setSelected(
                    dataModel.getDataAccessPermissions().contains(permission)
            );

            JLabel title = new JLabel(permission.getType().value());
            title.setFont(title.getFont().deriveFont(Font.BOLD));

            JLabel description = new JLabel(
                    permission.getDescription()
            );
            description.setForeground(UIUtil.getContextHelpForeground());

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            textPanel.add(title);
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(description);

            JPanel item = new JPanel(new BorderLayout(10, 0));
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.setOpaque(true);

            item.add(checkbox, BorderLayout.WEST);
            item.add(textPanel, BorderLayout.CENTER);

            Runnable updateItem = () -> {
                boolean selected = checkbox.isSelected();

                if (selected) {
                    dataModel.getDataAccessPermissions().add(permission);

                    item.setBorder(
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(
                                            UIUtil.getFocusedBorderColor()
                                    ),
                                    BorderFactory.createEmptyBorder(
                                            8, 10, 8, 10
                                    )
                            )
                    );
                } else {
                    dataModel.getDataAccessPermissions().remove(permission);

                    item.setBorder(
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(
                                            UIUtil.getLabelForeground()
                                    ),
                                    BorderFactory.createEmptyBorder(
                                            8, 10, 8, 10
                                    )
                            )
                    );
                }

                item.repaint();
            };

            checkbox.addActionListener(e -> updateItem.run());

            MouseAdapter listener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getSource() != checkbox) {
                        checkbox.setSelected(!checkbox.isSelected());
                    }
                }
            };

            item.addMouseListener(listener);
            title.addMouseListener(listener);
            description.addMouseListener(listener);
            textPanel.addMouseListener(listener);

            updateItem.run();

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.setBorder(
                    BorderFactory.createEmptyBorder(0, 0, 6, 0)
            );
            wrapper.add(item, BorderLayout.CENTER);

            checkboxPanel.add(wrapper);
        }

        return checkboxPanel;
    }

    private JPanel createResourcePanel() {
        JPanel resourcePanel = new JPanel(new BorderLayout());

        resourcePanel.setBorder(
                JBUI.Borders.compound(
                        JBUI.Borders.customLine(JBColor.border(), 1),
                        JBUI.Borders.empty(12)
                )
        );

        JBLabel resourceName =
                new JBLabel("https://litellm.example/v1");

        JBLabel modelName =
                new JBLabel("gpt-oss-120b");

        JPanel resourceInfo = new JPanel();
        resourceInfo.setOpaque(false);
        resourceInfo.setLayout(
                new BoxLayout(resourceInfo, BoxLayout.Y_AXIS)
        );

        resourceInfo.add(resourceName);
        resourceInfo.add(modelName);

        JBLabel status = new JBLabel("Available");

        resourcePanel.add(
                resourceInfo,
                BorderLayout.CENTER
        );

        resourcePanel.add(
                status,
                BorderLayout.EAST
        );

        return resourcePanel;
    }
}
