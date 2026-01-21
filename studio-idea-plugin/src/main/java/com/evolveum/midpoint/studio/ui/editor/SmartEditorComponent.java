/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.editor;

import com.evolveum.midpoint.studio.util.LanguageUtils;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SmartEditorComponent extends LanguageTextField {

    private final Project project;
    private Language currentLanguage;

    private final DocumentListener documentListener = new DocumentListener() {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            String text = event.getDocument().getText();
            Language detected = LanguageUtils.detectLanguage(text);

            if (detected != null && detected != currentLanguage) {
                switchLanguage(detected, text);
            }
        }
    };

    public SmartEditorComponent(
            @NotNull Project project,
            @NotNull Language initialLanguage,
            @NotNull String initialText
    ) {
        super(initialLanguage, project, initialText, false);
        this.project = project;
        this.currentLanguage = initialLanguage;
        getDocument().addDocumentListener(documentListener);

    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();

        editor.setHorizontalScrollbarVisible(true);
        editor.setVerticalScrollbarVisible(true);
        editor.getSettings().setLineNumbersShown(true);
        editor.getSettings().setLineMarkerAreaShown(true);
        editor.getSettings().setFoldingOutlineShown(true);
        editor.getSettings().setCaretRowShown(true);
        editor.getSettings().setRightMarginShown(true);
        editor.getSettings().setIndentGuidesShown(true);
        editor.getSettings().setUseSoftWraps(false);
        editor.getSettings().setAdditionalLinesCount(3);
        editor.getSettings().setAdditionalColumnsCount(3);

        editor.setHighlighter(HighlighterFactory.createHighlighter(project, Objects.requireNonNull(currentLanguage.getAssociatedFileType())));

        return editor;
    }

    public void switchLanguage(@NotNull Language language, @NotNull String text) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }

            currentLanguage = language;
            PsiFile psiFile = PsiFileFactory.getInstance(project)
                    .createFileFromText(
                            "SmartEditorComponentDummy." + currentLanguage.getAssociatedFileType().getDefaultExtension(),
                            currentLanguage,
                            text,
                            true,
                            false
                    );

            Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);

            if (document == null) return;

            PsiDocumentManager.getInstance(project).commitDocument(document);
            document.addDocumentListener(documentListener);
            setDocument(document);
            DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
        });
    }

    public void setViewer(boolean viewer) {
        super.setViewer(viewer);
    }
}
