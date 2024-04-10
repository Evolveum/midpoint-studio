package com.evolveum.midpoint.studio.impl.lang.annotation;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MappingType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

public class MappingAnnotator implements Annotator, MidPointAnnotator {

    private static final String MESSAGE =
            "Mapping doesn't have name defined.";

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Project project = element.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        XmlTag mapping = getMappingTypeTag(element);
        if (mapping == null) {
            return;
        }

        XmlTag name = MidPointUtils.findSubTag(mapping, MappingType.F_NAME);
        if (name != null && StringUtils.isNotEmpty(name.getValue().getText())) {
            return;
        }

        createTagAnnotations(mapping, holder, HighlightSeverity.WARNING, MESSAGE, null);
    }

    private XmlTag getMappingTypeTag(PsiElement element) {
        if (!(element instanceof XmlTag tag)) {
            return null;
        }

        QName type = PsiUtils.getTagXsdType(tag);
        if (type == null || !MappingType.COMPLEX_TYPE.equals(type)) {
            return null;
        }

        return tag;
    }
}
