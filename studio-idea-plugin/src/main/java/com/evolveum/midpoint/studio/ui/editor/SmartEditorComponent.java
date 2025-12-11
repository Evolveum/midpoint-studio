/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.editor;

import com.evolveum.midpoint.studio.util.LanguageUtils;
import com.intellij.codeInsight.completion.impl.CompletionServiceImpl;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class SmartEditorComponent extends JComponent {

    private final Project project;
    private LanguageTextField field;

    private final Timer typingStopTimer;

    public SmartEditorComponent(Project project, Language startLanguage) {
        this.project = project;
        setLayout(new BorderLayout());
        field = createField(startLanguage, "");
        add(field, BorderLayout.CENTER);

        configureEditor(field);

        typingStopTimer = new Timer(500, this::onStoppedTyping);
        typingStopTimer.setRepeats(false);

        addDocumentListener(field);
    }

    private void configureEditor(LanguageTextField field) {
        EditorEx editor = (EditorEx) field.getEditor();
        if (editor == null) {
            SwingUtilities.invokeLater(() -> configureEditor(field));
            return;
        }

        editor.getSettings().setLineNumbersShown(true);
        editor.getSettings().setFoldingOutlineShown(true);
        editor.getSettings().setIndentGuidesShown(true);
        editor.getSettings().setRightMarginShown(false);
        editor.getSettings().setWhitespacesShown(false);
        editor.getSettings().setAnimatedScrolling(true);
        editor.getSettings().setAutoCodeFoldingEnabled(true);

        editor.setVerticalScrollbarVisible(true);
        editor.setHorizontalScrollbarVisible(true);

        editor.getSettings().setUseSoftWraps(true);
        editor.getSettings().setRightMarginShown(false);
        editor.getSettings().setWhitespacesShown(true);
    }

    private LanguageTextField createField(Language lang, String content) {
        return new LanguageTextField(lang, project, content, false);
    }

    private void addDocumentListener(LanguageTextField f) {
        f.getDocument().addDocumentListener(new DocumentListener() {

            Language currentLang;

            @Override
            public void beforeDocumentChange(@NotNull DocumentEvent event) {
                currentLang = LanguageUtils.detectLanguage(f.getText());
            }

            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                String text = f.getText();
                Language detectedLang = LanguageUtils.detectLanguage(text);

                if (!currentLang.equals(detectedLang)) {
                    typingStopTimer.restart();
                }
            }
        });
    }

    private void onStoppedTyping(ActionEvent actionEvent) {
        String text = field.getText();
        Language newLang = LanguageUtils.detectLanguage(text);
        replaceField(newLang, text);
    }

    private void replaceField(Language lang, String text) {
        int caretOffset = 0;
        if (field.getEditor() != null) {
            caretOffset = field.getEditor().getCaretModel().getOffset();
        }

        remove(field);

        field = createField(lang, text);
        add(field, BorderLayout.CENTER);

        addDocumentListener(field);
        revalidate();
        repaint();

        final int finalCaretOffset = caretOffset;

        SwingUtilities.invokeLater(() -> {
            configureEditor(field);

            if (field.getEditor() != null) {
                field.getEditor().getCaretModel().moveToOffset(
                        Math.min(finalCaretOffset, field.getDocument().getTextLength())
                );

                field.getEditor().getContentComponent().requestFocusInWindow();
            }
        });

        IdeFocusManager.getInstance(project).requestFocus(
                Objects.requireNonNull(field.getEditor()).getContentComponent(), true
        );
    }

    public String getText() {
        return field.getText();
    }

    public void setText(String newContent) {
        if (field == null) return;

        Language detectedLanguage = LanguageUtils.detectLanguage(newContent);
        replaceField(detectedLanguage, newContent);
        moveCaretToEnd();
    }

    public void updateLanguage(Language language) {
        replaceField(language, this.field.getText());
    }

    public void setViewer(boolean viewer) {
        field.setViewer(viewer);
    }

    private void moveCaretToEnd() {
        SwingUtilities.invokeLater(() -> {
            if (field != null && field.getEditor() != null) {
                int length = field.getDocument().getTextLength();
                field.getEditor().getCaretModel().moveToOffset(length);
                field.getEditor().getScrollingModel().scrollToCaret(
                        com.intellij.openapi.editor.ScrollType.RELATIVE
                );
            }
        });
    }

    private void disableInspections(LanguageTextField field) {
        PsiFile psi = PsiDocumentManager.getInstance(project)
                .getPsiFile(field.getDocument());

        if (psi != null) {
            DaemonCodeAnalyzer.getInstance(project)
                    .setHighlightingEnabled(psi, false);
        }
    }

    public void disableCodeCompletion() {
        SwingUtilities.invokeLater(() -> {
            EditorEx ex = (EditorEx) field.getEditor();
            if (ex != null) {
                ex.putUserData(CompletionServiceImpl.FORBID_WORD_COMPLETION, Boolean.TRUE);
            }
        });
    }
}
