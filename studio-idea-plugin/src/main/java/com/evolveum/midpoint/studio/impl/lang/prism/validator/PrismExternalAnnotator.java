package com.evolveum.midpoint.studio.impl.lang.prism.validator;

import com.evolveum.concepts.ValidationLog;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.lang.prism.PrismIntentionAction;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;

import java.util.List;

public class PrismExternalAnnotator
        extends ExternalAnnotator<PrismExternalAnnotator.PrismAnnotationInfo, List<ValidationLog>> {

    public record PrismAnnotationInfo(
            PrismContext prismContext,
            Language language,
            String text
    ) {
    }

    @Override
    public @Nullable PrismAnnotationInfo collectInformation(
            @NotNull PsiFile file,
            @NotNull Editor editor,
            boolean hasErrors) {

        PrismContext prismContext =
                StudioPrismContextService.getPrismContext(file.getProject());

        return new PrismAnnotationInfo(
                prismContext,
                file.getLanguage(),
                editor.getDocument().getText()
        );
    }

    @Override
    public @Nullable List<ValidationLog> doAnnotate(
            PrismAnnotationInfo info) {

        var parsingCtx = info.prismContext()
                .createParsingContextForCompatibilityMode()
                .validation();

        try {
            RootXNode root = info.prismContext()
                    .parserFor(info.text())
                    .language(getPrismLanguage(info.language()))
                    .context(parsingCtx)
                    .parseToXNode();

            info.prismContext()
                    .parserFor(root)
                    .context(parsingCtx)
                    .parse();

        } catch (Exception ignore) {
            // TODO: decide whether parser exceptions should also be reported
        }

        return parsingCtx.getWarnings();
    }

    @Override
    public void apply(
            @NotNull PsiFile file,
            List<ValidationLog> annotationResult,
            @NotNull AnnotationHolder holder) {

        if (annotationResult == null) {
            return;
        }

        Language language = file.getLanguage();

        for (ValidationLog log : annotationResult) {

            PsiElement positionElement = null;

            if (log.location() != null) {
                int line = log.location().getLine();
                int column = log.location().getChar();

                var elementAtLineColumn =
                        PsiUtils.getElementAtLineColumn(
                                file,
                                line,
                                column
                        );

                if (language.isKindOf(XMLLanguage.INSTANCE)) {
                    positionElement =
                            PsiUtils.findXmlTagParent(elementAtLineColumn);

                } else if (language.isKindOf(JsonLanguage.INSTANCE)) {
                    positionElement =
                            PsiUtils.findJsonParent(elementAtLineColumn);

                } else if (language.isKindOf(YAMLLanguage.INSTANCE)) {
                    positionElement =
                            PsiUtils.findYamlKeyValueParent(elementAtLineColumn);
                }
            }

            HighlightSeverity severity = switch (log.validationLogType()) {
                case ERROR -> HighlightSeverity.ERROR;
                case WARNING -> HighlightSeverity.WARNING;
                default -> HighlightSeverity.ERROR;
            };

            if (positionElement != null) {
                holder.newAnnotation(
                                severity,
                                log.message()
                        )
                        .range(positionElement)
                        .withFix(new PrismIntentionAction(log))
                        .create();
            }
        }
    }

    private String getPrismLanguage(Language language) {
        if (language.isKindOf(XMLLanguage.INSTANCE)) {
            return XMLLanguage.INSTANCE.getID().toLowerCase();
        }

        if (language.isKindOf(JsonLanguage.INSTANCE)) {
            return JsonLanguage.INSTANCE.getID().toLowerCase();
        }

        if (language.isKindOf(YAMLLanguage.INSTANCE)) {
            return YAMLLanguage.INSTANCE.getID().toLowerCase();
        }

        throw new IllegalArgumentException(
                "Unsupported language: " + language
        );
    }
}