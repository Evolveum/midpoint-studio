/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.mapping;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.ui.editor.EditorPanel;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.WrapLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MappingSuggestionList extends JPanel {

    private final EditorPanel detailsArea;
    private final JPanel detailsPanel = new JPanel(new BorderLayout());

    List<MappingSuggestionList.SuggestionTile> tiles = new ArrayList<>();

    public MappingSuggestionList(Project project, PrismContext prismContext, ResourceType resource, MappingsSuggestionType mappingsSuggestionType) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        this.detailsArea = new EditorPanel(project, "", "xml");

        JPanel listPanel = new JPanel(new WrapLayout());
        listPanel.setBackground(JBColor.PanelBackground);

        for (AttributeMappingsSuggestionType attributeMappings : mappingsSuggestionType.getAttributeMappings()) {
            MappingSuggestionList.SuggestionTile tile = new MappingSuggestionList.SuggestionTile(attributeMappings);
            tile.setAlignmentX(Component.LEFT_ALIGNMENT);

            String rawXml = "";
            try {
                rawXml = prismContext.serializerFor(PrismContext.LANG_XML).serializeRealValue(attributeMappings);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize object", e);
            }
            tile.setRawXml(rawXml);

            listPanel.add(tile);
            listPanel.add(Box.createVerticalStrut(12));

            tiles.add(tile);
            tile.onToggle(e -> handleToggle(tile, tiles));

            tile.onApply(e -> handleApply(project, resource.getOid(), tile));
        }

        JPanel viewport = new JPanel(new BorderLayout());
        viewport.add(listPanel, BorderLayout.NORTH);

        detailsPanel.add(detailsArea, BorderLayout.CENTER);
        detailsPanel.setPreferredSize(new Dimension(700, 500));
        detailsPanel.setVisible(false);

        JBScrollPane scrollPane = new JBScrollPane(viewport);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(700, 350));
    }


    public static class SuggestionTile extends JPanel {

        private final JButton applyBtn;
        private final JButton showHideBtn;

        private String rawXml;

        public SuggestionTile(AttributeMappingsSuggestionType itemsSubCorrelatorType) {
            setLayout(new BorderLayout());
            setBorder(JBUI.Borders.empty(12));
            setOpaque(true);

            setBackground(JBColor.WHITE);
            setBorder(JBUI.Borders.customLine(JBColor.border(), 1, 1, 1, 1));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setOpaque(false);
            contentPanel.setBorder(JBUI.Borders.empty(12));
            add(contentPanel, BorderLayout.CENTER);

            JLabel title = new JLabel(itemsSubCorrelatorType.getDefinition().getDisplayName());
            title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
            JLabel subtitle = new JLabel(itemsSubCorrelatorType.getDefinition().getDescription());
            subtitle.setForeground(JBColor.GRAY);

            JPanel titlePanel = new JPanel();
            titlePanel.setOpaque(false);
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
            titlePanel.add(title);
            titlePanel.add(Box.createVerticalStrut(5));
            titlePanel.add(subtitle);

            contentPanel.add(titlePanel, BorderLayout.NORTH);

            JPanel pillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pillPanel.setOpaque(false);

//            itemsSubCorrelatorType.getItem().forEach(item -> {
//                JButton pill = new JButton(item.getName());
//                pill.setFocusable(false);
//                pill.setBorder(JBUI.Borders.empty(5, 10));
//                pillPanel.add(pill);
//                contentPanel.add(pillPanel, BorderLayout.CENTER);
//            });

            JPanel statsPanel = new JPanel();
            statsPanel.setOpaque(false);
            statsPanel.setBorder(JBUI.Borders.emptyTop(12));

//            if (itemsSubCorrelatorType.getComposition().getWeight() != null) {
//                statsPanel.add(createStatBox(itemsSubCorrelatorType.getComposition().getWeight().toString(), "Weight"));
//            }
//
//            if (itemsSubCorrelatorType.getComposition().getTier() != null) {
//                statsPanel.add(createStatBox(itemsSubCorrelatorType.getComposition().getTier().toString(), "Tier"));
//            }

//            statsPanel.add(createStatBox("100", "Efficiency"));

            contentPanel.add(statsPanel, BorderLayout.SOUTH);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            footer.setOpaque(false);
            this.applyBtn = new JButton("Apply");
            this.showHideBtn = new JButton("Show");
            footer.add(applyBtn);
            footer.add(showHideBtn);
            footer.setBorder(JBUI.Borders.empty(12));

            add(footer, BorderLayout.PAGE_END);
        }

        private JPanel createStatBox(String value, String label) {
            JPanel box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setOpaque(true);
            box.setBackground(new JBColor(new Color(246, 247, 248), Gray._60));
            box.setBorder(JBUI.Borders.empty(12));

            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 16f));

            JLabel labelLabel = new JLabel(label);
            labelLabel.setForeground(JBColor.GRAY);

            box.add(valueLabel);
            box.add(labelLabel);

            return box;
        }

        public String getRawXml() {
            return rawXml;
        }

        public void setRawXml(String rawXml) {
            this.rawXml = rawXml;
        }

        public void onToggle(ActionListener l) {
            showHideBtn.addActionListener(l);
        }

        public void setExpanded(boolean expanded) {
            showHideBtn.setText(expanded ? "Hide" : "Show");
        }

        public boolean isExpanded() {
            return showHideBtn.getText().equals("Hide");
        }

        public void onApply(ActionListener listener) {
            applyBtn.addActionListener(listener);
        }
    }

    private void handleToggle(MappingSuggestionList.SuggestionTile clickedTile, List<MappingSuggestionList.SuggestionTile> allTiles) {
        boolean willExpand = !clickedTile.isExpanded();

        for (MappingSuggestionList.SuggestionTile tile : allTiles) {
            tile.setExpanded(false);
        }

        if (willExpand) {
            clickedTile.setExpanded(true);
            detailsPanel.setVisible(true);

            ApplicationManager.getApplication().runWriteAction(() -> {
                detailsArea.setText(clickedTile.getRawXml());
            });
        } else {
            detailsPanel.setVisible(false);
        }

        revalidate();
        repaint();
    }

    private void handleApply(Project project, String resourceOid, MappingSuggestionList.SuggestionTile tile) {
        if (tile.getFocusTraversalKeysEnabled()) {
            PsiFile psiFile = MidPointUtils.findPsiFileByOid(project, resourceOid);
            if (psiFile instanceof XmlFile xmlFile) {
                XmlTag root = xmlFile.getRootTag();

                if (root == null || !root.getName().equals("resource")) {
                    MidPointUtils.publishNotification(
                            project,
                            "Apply generated suggestion",
                            "Apply generated suggestion",
                            "Object with oid '"  + resourceOid + "' is not a resource",
                            NotificationType.ERROR
                    );
                    return;
                }

                WriteCommandAction.runWriteCommandAction(project, () -> {
                    XmlElementFactory factory = XmlElementFactory.getInstance(project);

                    XmlTag correlations = root.findFirstSubTag("correlations");
                    if (correlations == null) {
                        correlations = factory.createTagFromText("<correlations/>");
                        correlations = root.addSubTag(correlations, false);
                    }

                    XmlTag correlation = correlations.findFirstSubTag("correlation");
                    if (correlation == null) {
                        correlation = factory.createTagFromText("<correlation/>");
                        correlation = correlations.addSubTag(correlation, false);
                    }

                    XmlTag newItemsXml = factory.createTagFromText(tile.getRawXml().trim());
                    correlation.addSubTag(newItemsXml, false);

                    Document doc = PsiDocumentManager.getInstance(project).getDocument(xmlFile);
                    if (doc != null) {
                        PsiDocumentManager.getInstance(project).commitDocument(doc);
                    }

                    tile.applyBtn.setEnabled(false);
                });
            } else {
                MidPointUtils.publishNotification(
                        project,
                        "Apply generated suggestion",
                        "Apply generated suggestion",
                        "File not found with oid '"  + resourceOid + "'",
                        NotificationType.ERROR
                );
            }
        }
    }
}
