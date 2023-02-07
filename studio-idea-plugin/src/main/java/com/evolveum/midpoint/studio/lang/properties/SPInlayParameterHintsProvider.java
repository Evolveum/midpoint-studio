package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.impl.Expander;
import com.evolveum.midpoint.studio.impl.cache.PropertiesInlayCacheService;
import com.intellij.codeInsight.hints.HintInfo;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPInlayParameterHintsProvider implements InlayParameterHintsProvider {

    @Override
    public @NotNull List<InlayInfo> getParameterHints(@NotNull PsiElement element) {
        if (!(element instanceof XmlText)) {
            return Collections.emptyList();
        }

        XmlText xmlText = (XmlText) element;
        String value = xmlText.getValue();
        if (value == null) {
            return Collections.emptyList();
        }

        Project project = element.getProject();

        if (project == null) {
            return Collections.emptyList();
        }

        PropertiesInlayCacheService cache = project.getService(PropertiesInlayCacheService.class);

        List<InlayInfo> result = new ArrayList<>();

        Matcher matcher = Expander.PATTERN.matcher(value);
        while (matcher.find()) {
            String key = matcher.group(1);
            if (key.isEmpty()) {
                continue;
            }

            int offset = element.getTextRange().getStartOffset() + matcher.regionStart();

            String label = cache.expandKeyForInlay(key, element.getContainingFile().getVirtualFile());
            if (label == null) {
                continue;
            }

            result.add(new InlayInfo(label, offset, false, true, false));
        }

        return result;
    }

    @Nullable
    @Override
    public HintInfo getHintInfo(@NotNull PsiElement element) {
        return null;
    }

    @NotNull
    @Override
    public Set<String> getDefaultBlackList() {
        return Collections.emptySet();
    }
}
