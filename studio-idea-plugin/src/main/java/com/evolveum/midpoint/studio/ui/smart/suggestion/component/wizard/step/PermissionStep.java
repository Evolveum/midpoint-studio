/*
 * Copyright (C) 2010-2026 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.step;

import com.evolveum.midpoint.smart.api.info.HealthStatus;
import com.evolveum.midpoint.studio.impl.LocalizationService;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionDataModel;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.GenerateSuggestionWizard;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AiInfoType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.DataAccessPermissionType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PermissionStep extends StepAdapter {

    private final GenerateSuggestionDataModel dataModel;

    private final LocalizationService localizationService = LocalizationService.get();

    public PermissionStep(
            GenerateSuggestionDataModel dataModel
    ) {
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
        title.setFont(JBFont.h3().asBold());
        content.add(title);

        JBLabel description = new JBLabel(
                "<html>" +
                        "To generate accurate suggestions, selected resource data " +
                        "may be sent to an external AI service for processing." +
                        "</html>"
        );

        description.setBorder(JBUI.Borders.empty(20, 0));
        content.add(description);

        JBLabel credentialsInfo = new JBLabel(
                "<html>" +
                        "No authentication credentials or user passwords will be shared." +
                        "</html>"
        );

        credentialsInfo.setBorder(JBUI.Borders.emptyBottom(20));
        content.add(credentialsInfo);

        JBLabel limitationInfo = new JBLabel(
                "<html>" +
                        "Without the selected data, suggestions may be limited to " +
                        "built-in heuristics or be unavailable." +
                        "</html>"
        );

        limitationInfo.setBorder(JBUI.Borders.emptyBottom(30));
        content.add(limitationInfo);

        if (dataModel.getAiInfo() != null) {
            content.add(createAiInfoPanel(dataModel.getAiInfo()));
        } else {
            JLabel errorLabel = new JLabel("Failed to retrieve AI info: Health endpoint returned non-success");
            errorLabel.setForeground(JBColor.RED);
            content.setFont(
                    UIUtil.getLabelFont().deriveFont(Font.BOLD)
            );
            content.add(errorLabel);
        }

        content.add(Box.createVerticalStrut(30));

        JBLabel optionsTitle = new JBLabel("Select which data can be used:");
        optionsTitle.setFont(JBFont.h4().asBold());

        content.add(optionsTitle);
        content.add(Box.createVerticalStrut(8));
        content.add(createOptionsPanel());

        return content;
    }

    public @NotNull JPanel createAiInfoPanel(@NotNull AiInfoType aiInfoType) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setOpaque(true);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                JBColor.GRAY
                        ),
                        JBUI.Borders.empty(20, 10)
                )
        );

        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.add(new JLabel(GenerateSuggestionWizard.Icons.Chip));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );
        textPanel.setOpaque(false);
        textPanel.setBorder(
                JBUI.Borders.emptyLeft(16)
        );

        JLabel urlLabel = new JLabel(aiInfoType.getProvider());
        urlLabel.setFont(
                urlLabel.getFont().deriveFont(Font.BOLD)
        );

        JLabel modelLabel = new JLabel(aiInfoType.getModel());
        modelLabel.setForeground(JBColor.GRAY);

        textPanel.add(urlLabel);
        textPanel.add(
                Box.createVerticalStrut(
                        JBUI.scale(4)
                )
        );
        textPanel.add(modelLabel);

        JPanel statusPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        JBUI.scale(6),
                        10
                )
        );
        statusPanel.setOpaque(false);

        switch (HealthStatus.fromString(aiInfoType.getHealthStatus())) {
            case OK: {
                var greenColor = new JBColor(
                        new Color(0x198754),
                        new Color(0x198754)
                );

                var status = new JLabel(
                        "Available",
                        new DotIcon(
                                12,
                                greenColor
                        ),
                        SwingConstants.LEFT
                );
                status.setFont(status.getFont().deriveFont(Font.BOLD));
                status.setForeground(greenColor);
                statusPanel.add(status);
                break;
            }
            case ERROR: {
                var redColor = new JBColor(
                        new Color(0xDC3545),
                        new Color(0xDC3545)
                );
                var status = new JLabel(
                        "Unavailable",
                        new DotIcon(
                                12,
                                redColor
                        ),
                        SwingConstants.LEFT
                );
                status.setFont(status.getFont().deriveFont(Font.BOLD));
                status.setForeground(redColor);
                statusPanel.add(status);
                break;
            }
            default: break;
        }

        panel.add(
                iconPanel,
                BorderLayout.WEST
        );

        panel.add(
                textPanel,
                BorderLayout.CENTER
        );

        panel.add(
                statusPanel,
                BorderLayout.EAST
        );

        return panel;
    }

    private @NotNull JComponent createOptionsPanel() {

        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));

        for (DataAccessPermissionType permission : DataAccessPermissionType.values()) {

            boolean hasRequiredPermission = switch (dataModel.getMode()) {
                case OBJECT_TYPE ->
                        permission == DataAccessPermissionType.SCHEMA_ACCESS ||
                                permission == DataAccessPermissionType.STATISTICS_ACCESS;

                case CORRELATION ->
                        permission == DataAccessPermissionType.SCHEMA_ACCESS;

                case MAPPING ->
                        permission == DataAccessPermissionType.SCHEMA_ACCESS ||
                                permission == DataAccessPermissionType.RAW_DATA_ACCESS;

                case ASSOCIATION ->
                        permission == DataAccessPermissionType.SCHEMA_ACCESS ||
                                permission == DataAccessPermissionType.STATISTICS_ACCESS ||
                                permission == DataAccessPermissionType.RAW_DATA_ACCESS;

                default -> true;
            };

            if (!hasRequiredPermission) {
                continue;
            }

            JCheckBox checkbox = new JCheckBox();
            checkbox.setSelected(true);

            var type = DataAccessPermission.fromType(permission);

            JLabel title = new JLabel(
                    localizationService.translate(type.getTitle())
            );
            title.setFont(title.getFont().deriveFont(Font.BOLD));

            JLabel description = new JLabel(
                    localizationService.translate(type.getDescription())
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
                                    JBUI.Borders.empty(10)
                            )
                    );
                } else {
                    dataModel.getDataAccessPermissions().remove(permission);

                    item.setBorder(
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(
                                            UIUtil.getLabelForeground()
                                    ),
                                    JBUI.Borders.empty(10)
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
                        updateItem.run();
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
                    JBUI.Borders.empty(10, 20, 10, 0)
            );
            wrapper.add(item, BorderLayout.CENTER);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

            checkboxPanel.add(wrapper);
        }

        JBScrollPane scrollPane = new JBScrollPane(checkboxPanel);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        );
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        return scrollPane;
    }

    public static class DotIcon implements Icon {
        private final int size;
        private final Color color;

        public DotIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2.setColor(color);
                g2.fillOval(x, y, size, size);
            } finally {
                g2.dispose();
            }
        }
    }
}
