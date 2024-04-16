package com.evolveum.midpoint.studio.impl.lang.inspection;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.PsiUtils;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class MissingNaturalKeyInspection extends LocalInspectionTool implements InspectionMixin {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                ProgressIndicatorProvider.checkCanceled();

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

                List<QName> naturalKeys = def.getNaturalKeyConstituents();
                if (naturalKeys == null || naturalKeys.isEmpty()) {
                    return;
                }

                for (QName key : naturalKeys) {
                    XmlTag subTag = MidPointUtils.findSubTag(tag, key);
                    if (subTag == null) {
                        registerTagProblems(tag, holder, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                "Missing key natural key constituent: " + key.getLocalPart());
                    }
                }
            }
        };
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
