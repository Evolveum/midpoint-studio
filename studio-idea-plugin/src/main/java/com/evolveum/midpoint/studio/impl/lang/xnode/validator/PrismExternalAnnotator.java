package com.evolveum.midpoint.studio.impl.lang.xnode.validator;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.TechnicalMessage;
import com.evolveum.concepts.ValidationLog;
import com.evolveum.concepts.ValidationLogType;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xnode.*;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.ValidationException;
import com.intellij.lang.Language;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik.
 */
public class PrismExternalAnnotator extends ExternalAnnotator<PsiElement, List<ValidationLog>> {

    PrismContext prismContext;
    Language language;

    @Override
    public @Nullable PsiElement collectInformation(@NotNull PsiFile file) {
        prismContext = StudioPrismContextService.getPrismContext(file.getProject());;
        language = file.getLanguage();
        return file;
    }

    @Override
    public @Nullable List<ValidationLog> doAnnotate(PsiElement element) {
        var parsingCtx = prismContext.createParsingContextForCompatibilityMode().validation();
        List<ValidationLog> annotationResult = new ArrayList<>();

        ApplicationManager.getApplication().runReadAction(() -> {
            try {
                RootXNode root = prismContext.parserFor(element.getText())
                        .language(language.getDisplayName().toLowerCase())
                        .context(parsingCtx)
                        .parseToXNode();

                prismContext.parserFor(root)
                        .context(parsingCtx)
                        .parse();

                annotationResult.addAll(parsingCtx.getValidationLogs());

            } catch (ValidationException e) {
                annotationResult.addAll(e.getValidationLogs());
            } catch (SchemaException e) {
                annotationResult.add(
                        new ValidationLog(
                                ValidationLogType.ERROR,
                                SourceLocation.unknown(),
                                new TechnicalMessage(""),
                                e.getMessage())
                );
            }
        });

        return annotationResult;
    }

    @Override
    public void apply(@NotNull PsiFile file, List<ValidationLog> annotationResult, @NotNull AnnotationHolder holder) {
        if (annotationResult == null) return;

        for (ValidationLog log : annotationResult) {
            PsiElement positionElement;



            if (log.location().equals(SourceLocation.unknown())) {
                System.out.println("AKSKKASKA:>>> " + log.location());
            }

            if (log.location() != null) {
                int line = log.location().getLine();
                int column = log.location().getChar();

                positionElement = getPsiElementAt(file.getProject(), file.getVirtualFile(),
                        line == 0 ? line : line -1,
                        column);
            } else {
                positionElement = file.getOriginalElement();
            }

//            HighlightSeverity severity = switch (log.validationLogType()) {
//                case ERROR -> HighlightSeverity.ERROR;
//                case WARNING -> HighlightSeverity.WARNING;
//                // if validationLogType == null highlight severity is error
//                default -> HighlightSeverity.ERROR;
//            };

            holder.newAnnotation(HighlightSeverity.ERROR, log.message())
                    .range(positionElement != null ? positionElement : file)
                    .create();
        }
    }

    private PsiElement getPsiElementAt(Project project, VirtualFile file, int line, int column) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) {
            return null;
        }

        Document document = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return null;
        }

        if (line < 0 || line >= document.getLineCount()) {
            return null;
        }
        int lineStartOffset = document.getLineStartOffset(line);
        int offset = lineStartOffset + column;

        return psiFile.findElementAt(offset);
    }
}
