package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryLangServiceImpl;
import com.evolveum.midpoint.prism.query.AxiomQueryLangService;
import com.evolveum.midpoint.studio.client.ServiceFactory;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Created by Dominik.
 */
public class AxiomQueryValidationExternalAnnotator
        extends ExternalAnnotator<PsiFile, List<AxiomQueryError>>
        implements AxiomQueryHints {

    AxiomQueryLangService axiomQueryLangService = new AxiomQueryLangServiceImpl(ServiceFactory.DEFAULT_PRISM_CONTEXT);

    @Override
    @Nullable
    public PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Nullable
    @Override
    public List<AxiomQueryError> doAnnotate(final PsiFile file) {
        String content = file.getText();
        if (content.isBlank()) {
            return Collections.emptyList();
        }

        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(file.getProject());
        Document doc = documentManager.getDocument(file);

        ItemDefinition<?> def = getItemDefinitionFromHint(doc);

        return axiomQueryLangService.validate(def, content);
    }

    @Override
    public void apply(@NotNull PsiFile file,
                      List<AxiomQueryError> errors,
                      @NotNull AnnotationHolder holder) {
        for (AxiomQueryError error : errors) {
            TextRange range = new TextRange(error.getCharPositionInLineStart(), error.getCharPositionInLineStop() + 1);
            holder.newAnnotation(HighlightSeverity.ERROR, error.getMessage())
                    .range(range)
                    .create();
        }
    }
}
