package com.evolveum.midpoint.studio.impl.lang.xnode.validator;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xnode.*;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.JsonToXNode;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.XNodeConverter;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.XmlToXNode;
import com.evolveum.midpoint.studio.impl.lang.xnode.converter.YamlToXNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.lang.Language;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.json.JsonLanguage;
import org.jetbrains.yaml.YAMLLanguage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik.
 */
public class XNodeExternalAnnotator extends ExternalAnnotator<PsiFile, List<XNodeError>> {

    PrismContext prismContext;
    XNodeConverter xNodeConverterImpl;

    @Override
    public @Nullable PsiFile collectInformation(@NotNull PsiFile file) {

        prismContext = StudioPrismContextService.getPrismContext(file.getProject());
        Language lang = file.getLanguage();

        if (lang.equals(XMLLanguage.INSTANCE)) {
            xNodeConverterImpl = new XmlToXNode();
        } else if (lang.equals(JsonLanguage.INSTANCE)) {
            xNodeConverterImpl = new JsonToXNode();
        } else if (lang.equals(YAMLLanguage.INSTANCE)) {
            xNodeConverterImpl = new YamlToXNode();
        }

        return file;
    }

    @Override
    public @Nullable List<XNodeError> doAnnotate(PsiFile psiFile) {
        List<XNodeError> annotationResult = new ArrayList<>();

        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                try {
                    XNode xNode = xNodeConverterImpl.convertFromPsi(element);

                    validate(xNode);

//                    annotationResult.addAll(validate(xNode));
                } catch (SchemaException e) {
//                    annotationResult.addAll(validate(xNode));
                    throw new RuntimeException(e);
                }

                super.visitElement(element);
            }
        });

        return annotationResult;
    }

    @Override
    public void apply(@NotNull PsiFile file, List<XNodeError> annotationResult, @NotNull AnnotationHolder holder) {
        if (annotationResult == null) return;

        for (XNodeError error : annotationResult) {
            int startOffset = error.position().charPositionInLineStart();
            int endOffset = error.position().charPositionInLineStop();

            if (startOffset >= 0 && endOffset > startOffset && endOffset <= file.getTextLength()) {
                holder.newAnnotation(HighlightSeverity.ERROR, error.msg())
                        .range(new TextRange(startOffset, endOffset))
                        .create();
            }
        }
    }

    private List<XNodeError> validate(XNode xNode) {

        if (xNode instanceof MapXNode map) {
            map.toMap().forEach((key, value) -> {
                validate(value);
            });
        } else if (xNode instanceof ListXNode list) {
            list.asList().forEach(item -> {
                // validate item of xNodeList
            });
        } else {
            // todo node.getClass().getSimpleName();
        }

        return null;

//        var parsingCtx = prismContext.createParsingContextForCompatibilityMode();
//        prismContext.parserFor(xNode.toRootXNode()).context(parsingCtx);
//
//        parsingCtx.getWarnings().forEach(error -> {
//            System.out.println("ERROR: " + error);
//        });
    }
}


