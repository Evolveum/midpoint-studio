package com.evolveum.midpoint.studio.impl.lang.annotation;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.XmlTagUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeprecatedElementAnnotator implements Annotator, MidPointAnnotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!PsiUtils.isXmlElement(element)) {
            return;
        }

        XmlElement xmlElement = (XmlElement) element;
        XmlTag xsd = PsiUtils.getXsdReference(xmlElement);
        if (xsd == null) {
            return;
        }

        XmlTag deprecatedTag = PsiUtils.getAppinfoElement(xsd, PrismConstants.A_DEPRECATED);
        XmlTag deprecatedSinceTag = PsiUtils.getAppinfoElement(xsd, PrismConstants.A_DEPRECATED_SINCE);

        String deprecated = deprecatedTag != null ? deprecatedTag.getValue().getText() : null;
        String deprecatedSince = deprecatedSinceTag != null ? deprecatedSinceTag.getValue().getText() : null;
        if (deprecatedSince == null) {
            deprecatedSince = "unknown";
        }

        if (!Boolean.valueOf(deprecated)) {
            return;
        }

        String msg = "Element marked as deprecated (since " + deprecatedSince + ")";
        String tooltip = "Element marked as deprecated (since <b>" + deprecatedSince + "</b>)";

        if (xmlElement instanceof XmlTag tag) {
            createNewAnnotation(XmlTagUtil.getStartTagNameElement(tag), holder, msg, tooltip);
            createNewAnnotation(XmlTagUtil.getEndTagNameElement(tag), holder, msg, tooltip);
        } else {
            createNewAnnotation(xmlElement, holder, msg, tooltip);
        }
    }

    private void createNewAnnotation(XmlElement element, AnnotationHolder holder, String msg, String tooltip) {
        holder.newAnnotation(HighlightSeverity.WARNING, msg)
                .range(element.getTextRange())
                .tooltip(tooltip)
                .highlightType(ProblemHighlightType.LIKE_DEPRECATED)
                .create();
    }
}
