package com.evolveum.midpoint.studio.impl.lang.codeInsight;

import com.evolveum.midpoint.studio.lang.properties.SPInlayParameterHintsProvider;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.codeInsight.hints.HintInfo;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointInlayParameterHintsProvider implements InlayParameterHintsProvider {

    private static final InlayParameterHintsProvider[] PROVIDERS = {
            new OidInlayParameterHintsProvider(),
            new SPInlayParameterHintsProvider()
    };

    @Override
    public @NotNull Set<String> getDefaultBlackList() {
        Set<String> set = new HashSet<>();

        for (InlayParameterHintsProvider p : PROVIDERS) {
            set.addAll(p.getDefaultBlackList());
        }

        return set;
    }

    @Override
    public @NotNull List<InlayInfo> getParameterHints(@NotNull PsiElement element) {
        List<InlayInfo> list = new ArrayList<>();

        Project project = element.getProject();
        if (!MidPointUtils.hasMidPointFacet(project)) {
            return list;
        }


        for (InlayParameterHintsProvider p : PROVIDERS) {
            list.addAll(p.getParameterHints(element));
        }

        return list;
    }

    @Nullable
    @Override
    public HintInfo getHintInfo(@NotNull PsiElement element) {
        return null;
    }
}
