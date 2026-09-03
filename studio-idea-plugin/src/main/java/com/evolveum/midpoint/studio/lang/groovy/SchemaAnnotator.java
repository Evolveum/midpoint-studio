package com.evolveum.midpoint.studio.lang.groovy;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Dominik.
 */
public class SchemaAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement.getText().equals("\"badValue\"")) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, "Invalid jsonType value")
                    .range(psiElement.getTextRange())
                    .create();
        }
    }
}
