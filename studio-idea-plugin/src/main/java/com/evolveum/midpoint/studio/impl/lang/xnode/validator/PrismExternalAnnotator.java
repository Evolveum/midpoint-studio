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
import com.intellij.json.JsonLanguage;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.Language;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.ArrayList;
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
        List<ValidationLog> annotationResult = new ArrayList<>();

        ApplicationManager.getApplication().runReadAction(() -> {
            try {
                RootXNode root = prismContext.parserFor(collectedInfo.getDocument().getText())
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
            PsiElement positionElement = null;

            if (log.location() != null && !log.location().equals(SourceLocation.unknown())) {
                int line = log.location().getLine();
                int column = log.location().getChar();
                var result = getElementAtLineColumn(file, line, column);

                if (language.isKindOf(XMLLanguage.INSTANCE)) {
                    positionElement = findXmlTagParent(result);
                } else if (language.isKindOf(JsonLanguage.INSTANCE)) {
                    positionElement = findJsonParent(result);
                } else if (language.isKindOf(YAMLLanguage.INSTANCE)) {
                    positionElement = findYamlKeyValueParent(result);
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
                        .create();
            }
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

    private PsiElement getElementAtLineColumn(PsiFile psiFile, int line, int column) {
        if (psiFile == null) return null;

        Document document = PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(psiFile);
        if (document == null) return null;

        int lineIndex = line - 1;
        int columnIndex = column - 1;

        if (lineIndex < 0 || lineIndex >= document.getLineCount()) return null;

        int lineStartOffset = document.getLineStartOffset(lineIndex);
        int offset = lineStartOffset + columnIndex;

        if (offset >= document.getTextLength()) offset = document.getTextLength() - 1;
        if (offset < 0) offset = 0;

        return psiFile.findElementAt(offset);
    }

    private XmlTag findXmlTagParent(PsiElement element) {
        if (element == null) {
            return null;
        }
        return PsiTreeUtil.getParentOfType(element, XmlTag.class, false);
    }

    private JsonProperty findJsonParent(PsiElement element) {
        if (element == null) {
            return null;
        }
        return PsiTreeUtil.getParentOfType(element, JsonProperty.class, false);
    }

    private YAMLValue findYamlKeyValueParent(PsiElement element) {
        if (element == null) {
            return null;
        }
        return PsiTreeUtil.getParentOfType(element, YAMLValue.class, false);
    }
}
