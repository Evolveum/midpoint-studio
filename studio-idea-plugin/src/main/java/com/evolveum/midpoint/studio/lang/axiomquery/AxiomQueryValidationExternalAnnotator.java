package com.evolveum.midpoint.studio.lang.axiomquery;

import com.evolveum.axiom.lang.antlr.AxiomQueryError;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.query.lang.AxiomQueryContentAssistImpl;
import com.evolveum.midpoint.prism.query.AxiomQueryContentAssist;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Dominik.
 */
public class AxiomQueryValidationExternalAnnotator
        extends ExternalAnnotator<PsiFile, List<AxiomQueryError>>
        implements AxiomQueryHints {

    @Override
    @Nullable
    public PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Nullable
    @Override
    public List<AxiomQueryError> doAnnotate(final PsiFile file) {
        Project project = file.getProject();

        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        Document doc = documentManager.getDocument(file);

        PrismContext ctx = StudioPrismContextService.getPrismContext(project);
        ItemDefinition<?> def = getItemDefinitionFromHint(doc,ctx);
        AxiomQueryContentAssist axiomQueryContentAssist = new AxiomQueryContentAssistImpl(ctx);
        return axiomQueryContentAssist.process(def, file.getText()).validate();
    }

    @Override
    public void apply(@NotNull PsiFile file,
                      List<AxiomQueryError> errors,
                      @NotNull AnnotationHolder holder) {
        for (AxiomQueryError error : errors) {
            TextRange range = new TextRange(error.charPositionInLineStart(), error.charPositionInLineStop());
            holder.newAnnotation(HighlightSeverity.ERROR, error.message())
                    .range(range)
                    .create();
        }
    }
}
