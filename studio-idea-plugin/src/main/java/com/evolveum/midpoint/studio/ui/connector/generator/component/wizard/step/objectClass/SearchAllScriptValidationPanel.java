package com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.objectClass;

import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.ConnectorGeneratorDialogContext;
import com.evolveum.midpoint.studio.ui.connector.generator.component.wizard.step.basic.DiscoverDocumentationPanel;
import com.evolveum.midpoint.studio.ui.dialog.wizard.WizardContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class SearchAllScriptValidationPanel extends JBPanel<DiscoverDocumentationPanel> implements WizardContent {

    public SearchAllScriptValidationPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(20));

        // 1. Header Section
        JBPanel<?> headerPanel = new JBPanel<>(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 10, true, false));

        headerPanel.add(new JBLabel("Search All Script Validation") {{
            setFont(JBUI.Fonts.label(18f));
        }});

        headerPanel.add(new JBLabel("<html>This step validates the Search All script to ensure it correctly retrieves " +
                "and processes all available objects from the target system.</html>") {{
            setForeground(UIUtil.getContextHelpForeground());
        }});

        add(headerPanel, BorderLayout.NORTH);

        // 2. Editor Section (Code Area)
        String scriptCode = "endpoint(\"/api/v3/users\") {\n" +
                "  // The API returns a HAL+JSON collection.\n" +
                "  objectExtractor {\n" +
                "    return response.body().get(\"_embedded\").get(\"elements\")\n" +
                "  }\n" +
                "  \n" +
                "  pagingSupport {\n" +
                "    request.queryParameter(\"pageSize\", paging.pageSize)\n" +
                "           .queryParameter(\"offset\", paging.pageOffset)\n" +
                "  }\n" +
                "}";

        add(createEditorContainer(scriptCode), BorderLayout.CENTER);
    }

    private JComponent createEditorContainer(String content) {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document document = editorFactory.createDocument(content);

        // Use Groovy FileType for the specific coloring seen in the image
        Editor editor = editorFactory.createEditor(document, null,
                FileTypeManager.getInstance().getFileTypeByExtension("groovy"), true);

        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setFoldingOutlineShown(true);
        settings.setIndentGuidesShown(true);
        settings.setLineMarkerAreaShown(true);
        settings.setRightMarginShown(false);

        JComponent editorComponent = editor.getComponent();

        // Wrap in a panel to provide the subtle border seen in the image
        JBPanel<?> wrapper = new JBPanel<>(new BorderLayout());
        wrapper.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1));
        wrapper.add(editorComponent, BorderLayout.CENTER);

        return wrapper;
    }

    @Override
    public void beforeChangeAction() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
