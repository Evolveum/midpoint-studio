package com.evolveum.midpoint.studio.lang.properties;

import com.evolveum.midpoint.studio.impl.Expander;
import com.evolveum.midpoint.studio.impl.cache.PropertiesInlayCacheService;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesInlayParameterHintsProvider implements InlayParameterHintsProvider {

    @Override
    public @NotNull List<InlayInfo> getParameterHints(@NotNull PsiElement element) {
        Project project = element.getProject();

        String value = null;
        if (element instanceof XmlText xmlText) {
            value = xmlText.getValue();
        } else if (element instanceof XmlAttributeValue xmlAttributeValue) {
            value = xmlAttributeValue.getValue();
        }

        if (value == null) {
            return Collections.emptyList();
        }

        PropertiesInlayCacheService cache = project.getService(PropertiesInlayCacheService.class);

        List<InlayInfo> result = new ArrayList<>();

        Matcher matcher = Expander.PATTERN.matcher(value);
        matcher.results().forEach(mr -> {
            String key = mr.group(1);
            if (key.isEmpty()) {
                return;
            }

            int offset = element.getTextRange().getStartOffset() + mr.start();

            String label = cache.expandKeyForInlay(key, element.getContainingFile().getVirtualFile());
            if (label == null) {
                return;
            }

            result.add(new InlayInfo(label, offset, false, true, false));
        });

        return result;
    }

    @NotNull
    @Override
    public Set<String> getDefaultBlackList() {
        return Collections.emptySet();
    }
}
