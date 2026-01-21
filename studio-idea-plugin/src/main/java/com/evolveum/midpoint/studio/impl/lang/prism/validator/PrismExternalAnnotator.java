package com.evolveum.midpoint.studio.impl.lang.prism.validator;

import com.evolveum.concepts.ValidationLog;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xnode.*;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.lang.prism.PrismIntentionAction;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;

import java.util.List;

/**
 * Created by Dominik.
 */
public class PrismExternalAnnotator extends ExternalAnnotator<Editor, List<ValidationLog>> {

    PrismContext prismContext;
    Language language;

    @Override
    public @Nullable Editor collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        prismContext = StudioPrismContextService.getPrismContext(file.getProject());
        language = file.getLanguage();
        return editor;
    }

    @Override
    public @Nullable List<ValidationLog> doAnnotate(Editor collectedInfo) {
        var parsingCtx = prismContext.createParsingContextForCompatibilityMode().validation();

        try {
            RootXNode root = prismContext.parserFor(collectedInfo.getDocument().getText())
                    .language(language.getDisplayName().toLowerCase())
                    .context(parsingCtx)
                    .parseToXNode();

            prismContext.parserFor(root)
                    .context(parsingCtx)
                    .parse();
        } catch (Exception ignore) {
        }

        return parsingCtx.getWarnings();
    }

    @Override
    public void apply(@NotNull PsiFile file, List<ValidationLog> annotationResult, @NotNull AnnotationHolder holder) {
        if (annotationResult == null) return;

        for (ValidationLog log : annotationResult) {
            PsiElement positionElement = null;

            if (log.location() != null) {
                int line = log.location().getLine();
                int column = log.location().getChar();
                var elementAtLineColumn = PsiUtils.getElementAtLineColumn(file, line, column);

                if (language.isKindOf(XMLLanguage.INSTANCE)) {
                    positionElement = PsiUtils.findXmlTagParent(elementAtLineColumn);
                } else if (language.isKindOf(JsonLanguage.INSTANCE)) {
                    positionElement = PsiUtils.findJsonParent(elementAtLineColumn);
                } else if (language.isKindOf(YAMLLanguage.INSTANCE)) {
                    positionElement = PsiUtils.findYamlKeyValueParent(elementAtLineColumn);
                }
            }

//            HighlightSeverity severity = switch (log.validationLogType()) {
//                case ERROR -> HighlightSeverity.ERROR;
//                case WARNING -> HighlightSeverity.WARNING;
//                // if validationLogType == null highlight severity is error
//                default -> HighlightSeverity.ERROR;
//            };

            if (positionElement != null) {
                holder.newAnnotation(HighlightSeverity.ERROR, log.message())
                        .range(positionElement)
                        .withFix(new PrismIntentionAction(log))
                        .create();
            }
        }
    }
}
