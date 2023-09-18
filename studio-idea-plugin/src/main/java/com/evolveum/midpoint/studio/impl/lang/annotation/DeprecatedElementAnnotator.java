package com.evolveum.midpoint.studio.impl.lang.annotation;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeprecatedElementAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Project project = element.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        if (element == null || !element.isValid()) {
            return;
        }

        PsiFile file = element.getContainingFile();
        if (!(file instanceof XmlFile)) {
            return;
        }

        if (!(element instanceof XmlElement)) {
            return;
        }

        XmlElement xmlElement = (XmlElement) element;

        PsiReference psiReference = xmlElement.getReference();
        if (!(psiReference instanceof TagNameReference)) {
            return;
        }

        TagNameReference ref = (TagNameReference) psiReference;
        PsiElement reference = ref.resolve();

        if (!(reference instanceof XmlTag)) {
            return;
        }

        XmlTag xsd = (XmlTag) reference;


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

        holder.newAnnotation(HighlightSeverity.WARNING, "Element marked as deprecated (since " + deprecatedSince + ")")
                .range(element.getTextRange())
                .tooltip("Element marked as deprecated (since <b>" + deprecatedSince + "</b>)")
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
