package com.evolveum.midpoint.studio.impl.lang.annotation;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowAssociationType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ShadowRefAnnotation implements Annotator, MidPointAnnotator {

    private static final String MESSAGE =
            "shadowRef element could represent possible problem when moving this midPoint " +
                    "object to another environment. Reason is reference to specific oid which would " +
                    "be different in another environment.";

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Project project = element.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        XmlTag shadowRef = getShadowRefTag(element);
        if (shadowRef == null) {
            return;
        }

        if (!hasValueExpressionAsParentTag(shadowRef)) {
            return;
        }

        createTagAnnotations(shadowRef, holder, HighlightSeverity.WARNING, MESSAGE, null);
    }

    private XmlTag getShadowRefTag(PsiElement element) {
        if (!(element instanceof XmlTag tag)) {
            return null;
        }

        if (!Objects.equals(ShadowAssociationType.F_SHADOW_REF, MidPointUtils.createQName(tag))) {
            return null;
        }

        return tag;
    }

    private boolean hasValueExpressionAsParentTag(XmlTag tag) {
        if (tag == null) {
            return false;
        }

        XmlTag parent = tag.getParentTag();

        return Objects.equals(SchemaConstantsGenerated.C_VALUE, MidPointUtils.createQName(parent));
    }
}
