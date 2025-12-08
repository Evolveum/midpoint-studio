/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.editor;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.json.JsonElementType;
import com.intellij.json.JsonElementTypes;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.json.highlighting.JsonSyntaxHighlighterFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaTokenType;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import groovy.json.JsonTokenType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Created by Dominik.
 */
public class EditorPanel extends JPanel {

    private final Project project;
    private EditorEx editor;

    public EditorPanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        setBorder(IdeBorderFactory.createBorder());
        Document document = EditorFactory.getInstance().createDocument("");
        editor = (EditorEx) EditorFactory.getInstance().createEditor(document, project);
        editor.getSettings().setLineNumbersShown(true);
        editor.getSettings().setFoldingOutlineShown(true);
        editor.getSettings().setIndentGuidesShown(true);
        editor.getSettings().setRightMarginShown(false);
        add(editor.getComponent(), BorderLayout.CENTER);
    }

    public EditorPanel(Project project, String content, @NotNull String lang) {
        super(new BorderLayout());
        this.project = project;
        setBorder(IdeBorderFactory.createBorder());
        init(content, lang);
    }

    private void init(String content, String lang) {
        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(lang);
        editor = (EditorEx) EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(content), project, fileType, false);
        editor.setHighlighter(HighlighterFactory.createHighlighter(project, fileType));
        editor.getSettings().setLineNumbersShown(true);
        editor.getSettings().setFoldingOutlineShown(true);
        editor.getSettings().setIndentGuidesShown(true);
        editor.getSettings().setRightMarginShown(false);
        add(editor.getComponent(), BorderLayout.CENTER);
    }

    public String getContent() {
        return editor.getDocument().getText();
    }

    public void setContent(String content) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            editor.getDocument().replaceString(0,
                    editor.getDocument().getTextLength(),
                    content
            );
        });
    }

    public void setViewer(boolean isOnlyViewer) {
        this.editor.setViewer(isOnlyViewer);
    }

    public void dispose() {
        EditorFactory.getInstance().releaseEditor(editor);
    }

    public DocumentEx getDocument() {
        return editor.getDocument();
    }

    public EditorEx getEditor() {
        return editor;
    }


    public void updateHighlighter(Language lang) {
//        SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(lang, project, null);
//        EditorColorsScheme colorsScheme = (EditorColorsScheme) EditorColorsManager.getInstance().getGlobalScheme().clone();
//        EditorHighlighter highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(syntaxHighlighter, colorsScheme);
//        editor.setHighlighter(highlighter);

        if (lang.equals(JsonLanguage.INSTANCE)) {



            EditorColorsScheme scheme = (EditorColorsScheme) EditorColorsManager.getInstance()
                    .getGlobalScheme()
                    .clone();

            TextAttributes attrs = scheme.getAttributes(JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY);
            if (attrs == null) {
                attrs = new TextAttributes();
            } else {
                attrs = attrs.clone();
            }

            attrs.setForegroundColor(JBColor.RED);      // foreground color
            attrs.setFontType(Font.BOLD);               // example: bold
            scheme.setAttributes(JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY, attrs);
            editor.setColorsScheme(scheme);
            editor.reinitSettings();
            editor.getContentComponent().repaint();

//            TextAttributes attrs = colorsScheme.getAttributes(JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY);



//            TextAttributes attrs1 = new TextAttributes(
//                    JBColor.RED,
//                    null,
//                    null,
//                    null,
//                    Font.PLAIN
//            );

//            editor.getColorsScheme().setColor(EditorColors.CARET_COLOR, JBColor.RED);
////            editor.getColorsScheme().setAttributes(JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY, attrs1);
//
//
//            TextAttributes attrs = editor.getColorsScheme()
//                    .getAttributes(JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY)
//                    .clone();
//
//            attrs.setForegroundColor(JBColor.RED);
//
//            editor.getColorsScheme().setAttributes(
//                    JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY,
//                    attrs
//            );
//
//            editor.getContentComponent().repaint();
//            editor.reinitSettings();
        }
    }

    public void updateHighlighter(FileType fileType) {
        editor.setHighlighter(HighlighterFactory.createHighlighter(
                project, fileType));
    }
}
