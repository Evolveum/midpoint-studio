package com.evolveum.midpoint.studio.impl.lang.inspection;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class StudioInspection extends LocalInspectionTool implements InspectionMixin {

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

                StudioInspection.this.visitElement(holder, isOnTheFly, element);
            }
        };
    }

    abstract void visitElement(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull PsiElement element);
}
