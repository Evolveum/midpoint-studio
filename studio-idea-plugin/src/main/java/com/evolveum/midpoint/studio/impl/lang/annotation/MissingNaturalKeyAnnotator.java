package com.evolveum.midpoint.studio.impl.lang.annotation;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.List;

public class MissingNaturalKeyAnnotator implements Annotator, MidPointAnnotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Project project = element.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        if (!(element instanceof XmlTag tag)) {
            return;
        }

        QName type = PsiUtils.getTagXsdType(tag);
        if (type == null) {
            return;
        }

        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        ComplexTypeDefinition def = ctx.getSchemaRegistry().findComplexTypeDefinitionByType(type);
        if (def == null) {
            return;
        }

        List<QName> naturalKeys = def.getNaturalKey();
        if (naturalKeys == null || naturalKeys.isEmpty()) {
            return;
        }

        for (QName key : naturalKeys) {
            XmlTag subTag = MidPointUtils.findSubTag(tag, key);
            if (subTag == null) {
                createTagAnnotations(tag, holder, HighlightSeverity.WARNING,
                        "Missing key natural key constituent: " + key.getLocalPart(), null);
            }
        }
    }
}
