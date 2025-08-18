package com.evolveum.midpoint.studio.impl.lang.xnode.converter;

import com.intellij.json.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Dominik.
 */
public class PsiToJsonConverter implements PsiConverter {

    @Override
    public @Nullable String convert(PsiElement element, boolean deep) {
        JsonObject jsonObject = findNearestJsonObject(element);
        if (jsonObject == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        buildJson(jsonObject, sb, 0, deep);
        return sb.toString();
    }

    private static JsonObject findNearestJsonObject(PsiElement element) {
        if (element instanceof JsonObject jsonObject) {
            return jsonObject;
        }
        return PsiTreeUtil.getParentOfType(element, JsonObject.class);
    }

    private static void buildJson(JsonObject obj, StringBuilder sb, int indent, boolean deep) {
        sb.append("{");
        boolean first = true;
        for (JsonProperty property : obj.getPropertyList()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\n").append("  ".repeat(indent + 1));
            sb.append("\"").append(property.getName()).append("\": ");

            if (deep) {
                JsonValue value = property.getValue();

                if (value instanceof JsonObject) {
                    buildJson((JsonObject) value, sb, indent + 1, deep);
                } else if (value != null) {
                    sb.append(value.getText());
                } else {
                    // null value
                }
            } else {
                sb.append("{}");
            }
        }
        sb.append("\n").append("  ".repeat(indent)).append("}");
    }
}
