package com.evolveum.midpoint.studio.ui.converter;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Dominik.
 */
public class EditorPanel extends JPanel {

    private final Project project;
    private Editor editor;

    public EditorPanel(Project project, String initialText, String langExtension) {
        super(new BorderLayout());
        this.project = project;
        setBorder(IdeBorderFactory.createBorder());
        setEditor(initialText, langExtension);
    }

    public void setEditor(String text, String langExtension) {
        if (editor != null) {
            EditorFactory.getInstance().releaseEditor(editor);
            removeAll();
        }

        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(langExtension);
        if (fileType == null) {
            fileType = FileTypeManager.getInstance().getFileTypeByExtension("txt");
        }

        Document document = EditorFactory.getInstance().createDocument(text);
        editor = EditorFactory.getInstance().createEditor(document, project, fileType, false);

        EditorEx editorEx = (EditorEx) editor;
        editorEx.getSettings().setLineNumbersShown(true);
        editorEx.getSettings().setFoldingOutlineShown(true);
        editorEx.getSettings().setIndentGuidesShown(true);
        editorEx.getSettings().setRightMarginShown(false);

        add(editor.getComponent(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public String getText() {
        return editor.getDocument().getText();
    }

    public void dispose() {
        EditorFactory.getInstance().releaseEditor(editor);
    }
}
