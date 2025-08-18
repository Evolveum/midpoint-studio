package com.evolveum.midpoint.studio.impl.lang.xnode.converter;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.*;

/**
 * Created by Dominik.
 */
public class PsiToYamlConverter implements PsiConverter {

    @Override
    public @Nullable String convert(PsiElement element, boolean deep) {
        YAMLMapping mapping = findNearestMapping(element);
        if (mapping == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        buildYaml(mapping, sb, 0, deep);
        return sb.toString();
    }

    private static YAMLMapping findNearestMapping(PsiElement element) {
        if (element instanceof YAMLMapping yamlMapping) {
            return yamlMapping;
        }
        return PsiTreeUtil.getParentOfType(element, YAMLMapping.class);
    }

    private static void buildYaml(YAMLMapping mapping, StringBuilder sb, int indent, boolean deep) {
        for (YAMLKeyValue kv : mapping.getKeyValues()) {
            indent(sb, indent);
            sb.append(kv.getKeyText()).append(": ");
            YAMLValue value = kv.getValue();

            if (deep) {
                if (value instanceof YAMLMapping) {
                    sb.append("\n");
                    buildYaml((YAMLMapping) value, sb, indent + 2, deep);
                } else if (value != null) {
                    sb.append(value.getText()).append("\n");
                } else {
                    // null value
                }
            }
        }
    }

    private static void indent(StringBuilder sb, int spaces) {
        sb.append(" ".repeat(spaces));
    }
}
