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
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class SchemaScriptValidatorPanel extends JBPanel<DiscoverDocumentationPanel> implements WizardContent {

    public SchemaScriptValidatorPanel(ConnectorGeneratorDialogContext context) {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(20));

        JBPanel<?> headerPanel = new JBPanel<>(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 10, true, false));
        headerPanel.add(new JBLabel("Schema Scripts Validation") {{
            setFont(JBUI.Fonts.label(18f));
        }});

        headerPanel.add(new JBLabel("<html>This step verifies that the schema scripts are correctly defined and executable, " +
                "ensuring the connector can properly interpret and process the data structure.</html>") {{
            setForeground(UIUtil.getContextHelpForeground());
        }});

        add(headerPanel, BorderLayout.NORTH);

        JBTabbedPane tabbedPane = new JBTabbedPane();

        String code = "objectClass(\"User\") {\n" +
                "  attribute(\"admin\") {\n" +
                "    jsonType \"boolean\";\n" +
                "    readable true;\n" +
                "    updateable true;\n" +
                "    returnedByDefault true;\n" +
                "  }\n" +
                "}";

        tabbedPane.addTab("User.native.schema.groovy", createEditorPanel(code));
        tabbedPane.addTab("User.cond.schema.groovy", createEditorPanel("// Conditional schema here"));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JComponent createEditorPanel(String content) {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document document = editorFactory.createDocument(content);
        Editor editor = editorFactory.createEditor(document, null,
                FileTypeManager.getInstance().getFileTypeByExtension("groovy"), false);

        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setFoldingOutlineShown(true);
        settings.setIndentGuidesShown(true);
        settings.setRightMarginShown(false);
        settings.setAdditionalLinesCount(3);

        JComponent component = editor.getComponent();
        component.setPreferredSize(new Dimension(800, 400));
        return component;
    }

    @Override
    public void beforeChangeAction() {

    }

    @Override
    public JBPanel<?> getPanel() {
        return this;
    }
}
