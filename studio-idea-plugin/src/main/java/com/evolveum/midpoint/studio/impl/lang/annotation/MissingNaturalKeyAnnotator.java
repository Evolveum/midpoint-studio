package com.evolveum.midpoint.studio.impl.lang.annotation;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
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
import java.util.ArrayList;
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

        PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;

        List<XmlTag> tags = listTagsToRoot(tag, new ArrayList<>());
        if (tags.isEmpty()) {
            return;
        }

        // todo suppose that this could be also c:objects not only specific object type
        XmlTag root = tags.get(0);
        QName rootType = PsiUtils.getTagXsdType(root);
        Class<?> rootTypeClass = ObjectTypes.getObjectTypeClassIfKnown(rootType);
        if (rootTypeClass == null) {
            return;
        }

        PrismObjectDefinition objectDefinition = ctx.getSchemaRegistry().findObjectDefinitionByType(rootType);
        if (objectDefinition == null) {
            return;
        }

        List<QName> path = toQNames(tags.subList(1, tags.size()));
        ItemDefinition def = objectDefinition.findItemDefinition(ItemPath.create(path));
        if (def == null || !def.isMultiValue() || !(def instanceof PrismContainerDefinition<?>)) {
            return;
        }

        List<QName> naturalKeys = null; //todo enable def.getNaturalKey();
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

    private List<QName> toQNames(List<XmlTag> tags) {
        return tags.stream()
                .map(MidPointUtils::createQName)
                .toList();
    }

    private List<XmlTag> listTagsToRoot(XmlTag tag, List<XmlTag> result) {
        result.add(0, tag);

        XmlTag parent = tag.getParentTag();
        return parent != null ? listTagsToRoot(parent, result) : result;
    }
}
