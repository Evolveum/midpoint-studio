/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.table;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.WrapLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class CorrelationRuleSuggestionList extends JPanel {

    private final SmartEditorComponent smartEditor;
    private final JPanel detailsPanel = new JPanel(new BorderLayout());

    List<SuggestionTile> tiles = new ArrayList<>();

    public CorrelationRuleSuggestionList(Project project, PrismContext prismContext, ResourceType resource, ResourceObjectTypeDefinitionType objectType, CorrelationSuggestionsType correlationSuggestionType) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        this.smartEditor = new SmartEditorComponent(project, XMLLanguage.INSTANCE);

        JPanel listPanel = new JPanel(new WrapLayout());
        listPanel.setBackground(JBColor.PanelBackground);

        for (CorrelationSuggestionType correlation : correlationSuggestionType.getSuggestion()) {
            for (ItemsSubCorrelatorType itemsSubCorrelatorType : correlation.getCorrelation().getCorrelators().getItems()) {
                SuggestionTile tile = new SuggestionTile(itemsSubCorrelatorType);
                tile.setAlignmentX(Component.LEFT_ALIGNMENT);

                String rawXml = "";
                try {
                    rawXml = prismContext.serializerFor(PrismContext.LANG_XML).serializeRealValue(itemsSubCorrelatorType);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to serialize object", e);
                }
                tile.setRawXml(rawXml);

                listPanel.add(tile);
                listPanel.add(Box.createVerticalStrut(12));

                tiles.add(tile);
                tile.onToggle(e -> handleToggle(tile, tiles));
                tile.onApply(e -> handleApply(project, resource.getOid(), objectType, tile));
            }
        }

        JPanel viewport = new JPanel(new BorderLayout());
        viewport.add(listPanel, BorderLayout.NORTH);

        detailsPanel.add(smartEditor, BorderLayout.CENTER);
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

        public SuggestionTile(ItemsSubCorrelatorType itemsSubCorrelatorType) {
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

            JLabel title = new JLabel(itemsSubCorrelatorType.getDisplayName());
            title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
            JLabel description = new JLabel(itemsSubCorrelatorType.getDescription());
            description.setForeground(JBColor.GRAY);

            JPanel titlePanel = new JPanel();
            titlePanel.setOpaque(false);
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
            titlePanel.add(title);
            titlePanel.add(Box.createVerticalStrut(5));
            titlePanel.add(description);

            contentPanel.add(titlePanel, BorderLayout.NORTH);

            JPanel pillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pillPanel.setOpaque(false);

//            itemsSubCorrelatorType.getItem().forEach(item -> {
//                JButton pill = new JButton(item.getRef().getItemPath().firstToVariableNameOrNull().getLocalPart());
//                pill.setFocusable(false);
//                pill.setBorder(JBUI.Borders.empty(5, 10));
//                pillPanel.add(pill);
//                contentPanel.add(pillPanel, BorderLayout.CENTER);
//            });

            JPanel statsPanel = new JPanel();
            statsPanel.setOpaque(false);
            statsPanel.setBorder(JBUI.Borders.emptyTop(12));

            if (itemsSubCorrelatorType.getComposition().getWeight() != null) {
                statsPanel.add(createStatBox(itemsSubCorrelatorType.getComposition().getWeight().toString(), "Weight"));
            }

            if (itemsSubCorrelatorType.getComposition().getTier() != null) {
                statsPanel.add(createStatBox(itemsSubCorrelatorType.getComposition().getTier().toString(), "Tier"));
            }

            statsPanel.add(createStatBox("100", "Efficiency"));

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

    private void handleToggle(SuggestionTile clickedTile, List<SuggestionTile> allTiles) {
        boolean willExpand = !clickedTile.isExpanded();

        for (SuggestionTile tile : allTiles) {
            tile.setExpanded(false);
        }

        if (willExpand) {
            clickedTile.setExpanded(true);
            detailsPanel.setVisible(true);

            ApplicationManager.getApplication().runWriteAction(() -> {
                smartEditor.setText(clickedTile.getRawXml());
            });
        } else {
            detailsPanel.setVisible(false);
        }

        revalidate();
        repaint();
    }

    private void handleApply(Project project, String resourceOid, ResourceObjectTypeDefinitionType objectType, SuggestionTile tile) {
        if (tile.getFocusTraversalKeysEnabled()) {
            PsiFile psiFile = MidPointUtils.findPsiFileByOid(project, resourceOid);
            if (psiFile instanceof XmlFile xmlFile) {
                XmlTag root = xmlFile.getRootTag();
                if (root == null || !root.getName().equals("resource")) {
                    MidPointUtils.publishNotification(
                            project,
                            "Apply generated suggestion",
                            "Apply generated suggestion",
                            "Object with oid '" + resourceOid + "' is not a resource",
                            NotificationType.ERROR
                    );
                    return;
                }

                XmlTag objectTypeElement = MidPointUtils.findObjectTypeById(root, objectType.getId().toString());

//                WriteCommandAction.runWriteCommandAction(project, () -> {
//                    XmlElementFactory factory = XmlElementFactory.getInstance(project);
//
//                    assert objectTypeElement != null;
//                    XmlTag correlations = objectTypeElement.findFirstSubTag("correlation");
//                    if (correlations == null) {
//                        correlations = factory.createTagFromText("<correlation/>");
//                        correlations = objectTypeElement.addSubTag(correlations, false);
//                    }
//
//                    XmlTag correlation = correlations.findFirstSubTag("correlator");
//                    if (correlation == null) {
//                        correlation = factory.createTagFromText("<correlator/>");
//                        correlation = correlations.addSubTag(correlation, false);
//                    }
//
//                    XmlTag newItemsXml = factory.createTagFromText(tile.getRawXml().trim());
//                    correlation.addSubTag(newItemsXml, false);
//
//                    Document doc = PsiDocumentManager.getInstance(project).getDocument(xmlFile);
//                    if (doc != null) {
//                        PsiDocumentManager.getInstance(project).commitDocument(doc);
//                    }
//
//                    tile.applyBtn.setEnabled(false);
//                });


                WriteCommandAction.runWriteCommandAction(project, () -> {
                    XmlElementFactory factory = XmlElementFactory.getInstance(project);

                    assert objectTypeElement != null;

                    XmlTag correlations = objectTypeElement.findFirstSubTag("correlation");
                    if (correlations == null) {
                        correlations = factory.createTagFromText("<correlation/>");
                        correlations = objectTypeElement.addSubTag(correlations, false);
                    }

                    XmlTag correlation = correlations.findFirstSubTag("correlator");
                    if (correlation == null) {
                        correlation = factory.createTagFromText("<correlator/>");
                        correlation = correlations.addSubTag(correlation, false);
                    }

                    String rawXml = tile.getRawXml().trim();
                    XmlTag newItemsXml = factory.createTagFromText(rawXml);
                    int startOffset = correlation.getTextRange().getEndOffset();
                    XmlTag addedTag = correlation.addSubTag(newItemsXml, false);
                    int endOffset = addedTag.getTextRange().getEndOffset();
                    Document doc = PsiDocumentManager.getInstance(project).getDocument(xmlFile);

                    if (doc != null) {
                        PsiDocumentManager.getInstance(project).commitDocument(doc);
                    }

                    tile.applyBtn.setEnabled(false);

                    ApplicationManager.getApplication().invokeLater(() -> {
                        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                        if (editor == null) return;

                        MarkupModel markup = editor.getMarkupModel();
                        TextAttributes attrs = EditorColorsManager.getInstance()
                                .getGlobalScheme()
                                .getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);

                        RangeHighlighter highlighter = markup.addRangeHighlighter(
                                startOffset,
                                endOffset,
                                HighlighterLayer.SELECTION - 1,
                                attrs,
                                HighlighterTargetArea.EXACT_RANGE
                        );

                        Runnable removeHighlighter = () -> {
                            if (highlighter.isValid()) {
                                markup.removeHighlighter(highlighter);
                            }
                        };

                        DocumentListener docListener = new DocumentListener() {
                            @Override
                            public void beforeDocumentChange(@NotNull DocumentEvent event) {
                                removeHighlighter.run();
                            }
                        };
                        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(docListener, project);

                        MessageBusConnection connection = project.getMessageBus().connect();
                        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                            @Override
                            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                                VirtualFile currentFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
                                if (currentFile != null && currentFile.equals(file)) {
                                    removeHighlighter.run();
                                    connection.disconnect();
                                }
                            }
                        });

                        editor.getCaretModel().moveToOffset(startOffset);
                        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                    });
                });

            } else {
                MidPointUtils.publishNotification(
                        project,
                        "Apply generated suggestion",
                        "Apply generated suggestion",
                        "File not found with oid '" + resourceOid + "'",
                        NotificationType.ERROR
                );
            }
        }
    }
}