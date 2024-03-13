package com.evolveum.midpoint.studio.impl.lang.annotation;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
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

        XmlTag annotation = getFirstSubTag(xsd, "annotation", SchemaConstantsGenerated.NS_XSD);
        if (annotation == null) {
            return;
        }

        XmlTag appinfo = getFirstSubTag(annotation, "appinfo", SchemaConstantsGenerated.NS_XSD);
        if (appinfo == null) {
            return;
        }

        String deprecated = getSubTagValue(appinfo, "deprecated", SchemaConstantsGenerated.NS_ANNOTATION);
        String deprecatedSince = getSubTagValue(appinfo, "deprecatedSince", SchemaConstantsGenerated.NS_ANNOTATION);
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

    private XmlTag getFirstSubTag(XmlTag tag, String localName, String namespace) {
        XmlTag[] tags = tag.findSubTags(localName, namespace);
        if (tags == null || tags.length == 0) {
            return null;
        }

        return tags[0];
    }


    private String getSubTagValue(XmlTag tag, String localName, String namespace) {
        XmlTag[] tags = tag.findSubTags(localName, namespace);
        if (tags == null || tags.length == 0) {
            return null;
        }

        XmlTagValue value = tags[0].getValue();
        return value != null ? value.getTrimmedText() : null;
    }

}
