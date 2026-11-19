package com.evolveum.midpoint.studio.lang.mel.impl;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Validates injected MEL fragments. DumbAware: MEL needs no indexes, and the platform skips
 * annotators, injectors and completion contributors that are not DumbAware while indexing.
 */
public class MelAnnotator implements Annotator, DumbAware {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof PsiFile file)) {
            return;
        }

        List<ValidationMessage> errors = MelValidator.validate(file.getText());
        for (var err : errors) {
            HighlightSeverity severity = switch (err.severity()) {
                case ERROR -> HighlightSeverity.ERROR;
                case WARNING -> HighlightSeverity.WARNING;
                case INFO -> HighlightSeverity.INFORMATION;
            };

            TextRange range = tokenToTextRange(err.token());
            holder.newAnnotation(severity, err.message())
                    .range(range)
                    .create();
        }
    }

    private TextRange tokenToTextRange(Token token) {
        int start = token.getStartIndex();
        int stop = token.getStopIndex() + 1;
        return new TextRange(start, stop);
    }
}
