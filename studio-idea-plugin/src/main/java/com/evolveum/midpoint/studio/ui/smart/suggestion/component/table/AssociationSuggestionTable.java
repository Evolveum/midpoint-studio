package com.evolveum.midpoint.studio.ui.smart.suggestion.component.table;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.ui.editor.SmartEditorComponent;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.dialog.GenerateSuggestionDialogContext;
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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AssociationSuggestionTable extends JPanel {

    private final SmartEditorComponent smartEditor;
    private final JPanel detailsPanel = new JPanel(new BorderLayout());

    public AssociationSuggestionTable(
            Project project,
            PrismContext prismContext,
            ResourceType resource,
            ResourceObjectTypeDefinitionType objectType,
            List<ResourceObjectAssociationType> objectAssociationTypeList,
            GenerateSuggestionDialogContext.Direction direction
    ) {
        setLayout(new BorderLayout());
        AssociationSuggestionTable.SuggestionTableModel model = new SuggestionTableModel();
        this.smartEditor =  new SmartEditorComponent(project, XMLLanguage.INSTANCE, "");
//        associationSuggestionType.getDefinition().getAssociationObject().getAssociation()
        for (ResourceObjectAssociationType o : objectAssociationTypeList) {
            String rawXml = "";
            try {
                rawXml = prismContext.serializerFor(PrismContext.LANG_XML).serializeRealValue(o);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize object", e);
            }

            model.addRow(new Item(o, rawXml, direction));
        }

        JBTable table = new JBTable(model);
        table.setRowHeight(30);

        TableColumn activityColumn = table.getColumnModel().getColumn(3);
        activityColumn.setCellRenderer(new ButtonRenderer());
        activityColumn.setCellEditor(new ApplyButtonEditor(new JCheckBox(), model, project, resource, objectType));

        TableColumn toggleColumn = table.getColumnModel().getColumn(4);
        toggleColumn.setCellRenderer(new ButtonRenderer());
        toggleColumn.setCellEditor(new ToggleButtonEditor(new JCheckBox(), project, model, this));

        detailsPanel.add(smartEditor, BorderLayout.CENTER);
        detailsPanel.setPreferredSize(new Dimension(700, 500));
        detailsPanel.setVisible(false);

        add(new JBScrollPane(table), BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(700, 350));
    }

    static class Item {
        GenerateSuggestionDialogContext.Direction direction;
        ResourceObjectAssociationType object;
        String rawCode;
        boolean applied = false;
        boolean expanded = false;

        Item(ResourceObjectAssociationType object, String xml, GenerateSuggestionDialogContext.Direction direction) {
            this.object = object;
            this.rawCode = xml;
            this.direction = direction;
        }
    }

    static class SuggestionTableModel extends AbstractTableModel {
        private final String[] columns = {"Association type", "Ref", "Kind", "Intent", "Direction", "Attribute", "Activity", "Details"};
        private final List<AssociationSuggestionTable.Item> data = new ArrayList<>();

        public void addRow(AssociationSuggestionTable.Item item) {
            data.add(item);
        }

        public AssociationSuggestionTable.Item getItemAt(int row) {
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
            AssociationSuggestionTable.Item item = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.object.getDisplayName();
                case 1 -> item.object.getRef().getItemPath().toString();
                case 2 -> item.object.getKind();
                case 3 -> item.object.getIntent();
                case 4 -> item.object.getDirection().toString();
                case 5 -> item.object.getAssociationAttribute().getLocalPart();
                case 6 -> item.applied ? "Applied" : "Apply";
                case 7 -> item.expanded ? "Hide" : "Show";
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 3 || col == 4;
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

        public ApplyButtonEditor(JCheckBox checkBox, AssociationSuggestionTable.SuggestionTableModel model, Project project, ResourceType resource, ResourceObjectTypeDefinitionType objectType) {
            super(checkBox);
            this.button = new JButton();
            this.button.setOpaque(true);

            this.button.addActionListener(e -> {
                fireEditingStopped();
                if (currentRow >= 0) {
                    AssociationSuggestionTable.Item item = model.getItemAt(currentRow);
                    if (!item.applied) {
//                        PsiFile psiFile = MidPointUtils.findPsiFileByOid(project, resource.getOid());
                        PsiFile psiFile = null;
                        if (psiFile instanceof XmlFile xmlFile) {
                            XmlTag root = xmlFile.getRootTag();

                            if (root == null || !root.getName().equals("resource")) {
                                MidPointUtils.publishNotification(
                                        project,
                                        "Apply generated suggestion",
                                        "Apply generated suggestion",
                                        "Object with oid '"  + resource.getOid() + "' is not a resource",
                                        NotificationType.ERROR
                                );
                                return;
                            }

                            XmlTag objectTypeElement = MidPointUtils.findObjectTypeById(root, objectType.getId().toString());

                            // TODO refactoring use PRISM for append generated <association> to objectType

                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                XmlElementFactory factory = XmlElementFactory.getInstance(project);

                                assert objectTypeElement != null;

                                XmlTag attribute = objectTypeElement.findFirstSubTag("attribute");
                                if (attribute == null) {
                                    attribute = factory.createTagFromText("<attribute/>");
                                    attribute = objectTypeElement.addSubTag(attribute, false);
                                }

                                String rawXml = item.rawCode.trim();
                                XmlTag newItemsXml = factory.createTagFromText(rawXml);
                                int startOffset = attribute.getTextRange().getEndOffset();
                                XmlTag addedTag = attribute.addSubTag(newItemsXml, false);
                                int endOffset = addedTag.getTextRange().getEndOffset();
                                Document doc = PsiDocumentManager.getInstance(project).getDocument(xmlFile);

                                if (doc != null) {
                                    PsiDocumentManager.getInstance(project).commitDocument(doc);
                                }

                                item.applied = false;

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
                                    "File not found with oid '"  + resource.getOid() + "'",
                                    NotificationType.ERROR
                            );
                        }
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
        private final AssociationSuggestionTable.SuggestionTableModel model;

        public ToggleButtonEditor(
                JCheckBox checkBox,
                Project project,
                AssociationSuggestionTable.SuggestionTableModel model,
                AssociationSuggestionTable parent
        ) {
            super(checkBox);
            this.model = model;

            this.button = new JButton();
            this.button.setOpaque(true);

            this.button.addActionListener(e -> {
                fireEditingStopped();

                if (currentRow < 0) return;

                AssociationSuggestionTable.Item clickedItem = model.getItemAt(currentRow);

                for (int i = 0; i < model.getRowCount(); i++) {
                    if (i != currentRow) {
                        AssociationSuggestionTable.Item it = model.getItemAt(i);
                        if (it.expanded) {
                            it.expanded = false;
                            parent.hideDetails();
                            model.fireTableRowsUpdated(i, i);
                        }
                    }
                }

                clickedItem.expanded = !clickedItem.expanded;
                model.fireTableRowsUpdated(currentRow, currentRow);

                if (clickedItem.expanded) {
                    parent.showDetails(clickedItem, project);
                } else {
                    parent.hideDetails();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column
        ) {
            currentRow = row;
            AssociationSuggestionTable.Item item = model.getItemAt(row);
            button.setText(item.expanded ? "Hide" : "Show");
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }


    public void showDetails(AssociationSuggestionTable.Item item, Project project) {
        if (item == null) return;

        WriteCommandAction.runWriteCommandAction(project, () -> {
            smartEditor.setText(item.rawCode != null ? item.rawCode : "");
        });
        smartEditor.setViewer(true);
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
