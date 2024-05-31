package com.evolveum.midpoint.studio.impl.lang.inspection;

import com.evolveum.midpoint.studio.impl.lang.codeInsight.EnumerationRefCompletionContributor;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LookupTableType;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnumerationRefInspection extends LocalInspectionTool implements InspectionMixin {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);

                Project project = element.getProject();
                if (!MidPointUtils.hasMidPointFacet(project)) {
                    return;
                }

                if (!(element instanceof XmlText xmlText)) {
                    return;
                }

                XmlTag tag = xmlText.getParentTag();
                LookupTableType table = EnumerationRefCompletionContributor.getLookupTableForTag(tag);
                if (table == null) {
                    return;
                }

                String value = xmlText.getText();
                List<String> allowed = table.getRow().stream()
                        .map(r -> r.getKey())
                        .toList();

                if (allowed.contains(value)) {
                    return;
                }

                holder.registerProblem(
                        xmlText,
                        "Value is not in the list of allowed values ("
                                + StringUtils.join(allowed, ", ") + ")",
                        ProblemHighlightType.ERROR);
            }
        };
    }
}
