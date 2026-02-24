/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class SmartSuggestionToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Content content = ContentFactory.getInstance().createContent(new SmartSuggestionToolWindowPreview(project), "Smart Suggestion", true);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public void init(ToolWindow window) {
        window.setStripeTitle("Smart Suggestion");
        window.setShowStripeButton(true);
        window.setTitle("Smart Suggestion");
    }

    static private class SmartSuggestionToolWindowPreview extends JPanel {

        final String SMART_SUGGESTION_GROUP_ACTION_ID = "MidPoint.Group.Menu.TransferRelated.SmartSuggestion";

        Project project;

        public SmartSuggestionToolWindowPreview(Project project) {
            this.project = project;
            setLayout(new BorderLayout());
            setBorder(JBUI.Borders.empty(20));
            setBackground(UIUtil.getPanelBackground());

            add(createMainContent(), BorderLayout.CENTER);
        }

        private JComponent createMainContent() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            JLabel title = new JLabel("MidPoint - Generate Smart Suggestions");
            title.setFont(JBFont.h1().asBold());
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            panel.add(title);
            panel.add(Box.createVerticalStrut(10));

            JLabel subtitle = new JLabel("Generating smart suggestions for Midpoint configuration using AI");
            subtitle.setForeground(JBColor.GRAY);
            subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            panel.add(subtitle);
            panel.add(Box.createVerticalStrut(20));

            JLabel featuresLabel = new JLabel("Features");
            featuresLabel.setFont(JBFont.h2().asBold());
            featuresLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            panel.add(featuresLabel);
            panel.add(Box.createVerticalStrut(10));

            ActionManager actionManager = ActionManager.getInstance();
            ActionGroup group = (ActionGroup) actionManager.getAction(SMART_SUGGESTION_GROUP_ACTION_ID);
            AnActionEvent event = AnActionEvent.createFromDataContext(
                    ActionPlaces.UNKNOWN,
                    null,
                    DataContext.EMPTY_CONTEXT
            );

            for (AnAction child : group.getChildren(event)) {
                JComponent itemComponent = featureItem(project, child);
                // FIXME description text responsive
                itemComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
                panel.add(itemComponent);
            }

            return panel;
        }

        private JComponent featureItem(Project project, AnAction action) {
            var presentation = action.getTemplatePresentation();

            JPanel row = new JPanel();
            row.setOpaque(false);
            row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));

            ActionLink titleLink = new ActionLink(presentation.getText(), e -> {
                DataContext context = SimpleDataContext.builder()
                        .add(CommonDataKeys.PROJECT, project)
                        .build();

                AnActionEvent event = AnActionEvent.createFromAnAction(
                        action,
                        null,
                        ActionPlaces.UNKNOWN,
                        context
                );

                action.update(event);

                if (event.getPresentation().isEnabled()) {
                    action.actionPerformed(event);
                }
            });
            titleLink.setFont(JBFont.medium());
            titleLink.setAlignmentX(Component.LEFT_ALIGNMENT);

            JBTextArea description = new JBTextArea(presentation.getDescription());
            description.setFont(JBFont.small());
            description.setForeground(JBColor.GRAY);
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setEditable(false);
            description.setAlignmentX(Component.LEFT_ALIGNMENT);

            row.add(titleLink);
            row.add(description);

            return row;
        }
    }
}
