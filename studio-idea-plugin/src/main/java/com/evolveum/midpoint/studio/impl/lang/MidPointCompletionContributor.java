package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.DefaultCompletionContributor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Parent class for all contributors, checks for midPoint facet.
 */
public abstract class MidPointCompletionContributor extends DefaultCompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (parameters.getEditor().getProject() == null) {
            return;
        }

        Project project = parameters.getEditor().getProject();

        if (!MidPointUtils.hasMidPointFacet(project)) {
            return;
        }

        super.fillCompletionVariants(parameters, result);
    }
}
