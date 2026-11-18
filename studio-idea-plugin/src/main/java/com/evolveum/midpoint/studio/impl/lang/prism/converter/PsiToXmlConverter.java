package com.evolveum.midpoint.studio.impl.lang.prism.converter;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dominik.
 */
public class PsiToXmlConverter implements PsiConverter {

    @Override
    public @Nullable String convert(PsiElement element, boolean deep) {
        XmlTag tag = findNearestTag(element);
        if (tag == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        buildXml(tag, sb, deep);
        return sb.toString();
    }

    private XmlTag findNearestTag(PsiElement element) {
        if (element instanceof XmlTag) {
            return (XmlTag) element;
        }
        return com.intellij.psi.util.PsiTreeUtil.getParentOfType(element, XmlTag.class);
    }

    private void buildXml(XmlTag tag, StringBuilder sb, boolean deep) {
        buildXml(tag, sb, deep, new HashMap<>());
    }

    /**
     * declared holds prefix to namespace pairs already written to the output, so a fragment cut from the
     * middle of a document gets the declarations its prefixes need and nested tags do not repeat them
     */
    private void buildXml(XmlTag tag, StringBuilder sb, boolean deep, Map<String, String> declared) {
        Map<String, String> scope = new HashMap<>(declared);
        for (XmlAttribute attr : tag.getAttributes()) {
            if ("xmlns".equals(attr.getName())) {
                scope.put("", attr.getValue());
            } else if ("xmlns".equals(attr.getNamespacePrefix())) {
                scope.put(attr.getLocalName(), attr.getValue());
            }
        }

        sb.append("<").append(tag.getName());

        declarePrefix(tag, tag.getNamespacePrefix(), scope, sb);
        for (XmlAttribute attr : tag.getAttributes()) {
            declarePrefix(tag, attr.getNamespacePrefix(), scope, sb);
        }

        for (XmlAttribute attr : tag.getAttributes()) {
            sb.append(" ")
                .append(attr.getName())
                .append("=\"")
                .append(attr.getValue())
                .append("\"");
        }

        XmlTag[] children = tag.getSubTags();
        String textValue = tag.getValue().getTrimmedText();

        if (children.length == 0 && textValue.isEmpty()) {
            sb.append("/>");
            return;
        }

        sb.append(">");

        if (deep) {
            if (children.length == 0) {
                sb.append(textValue);
            } else {
                for (XmlTag child : children) {
                    buildXml(child, sb, deep, scope);
                }
            }
        }

        sb.append("</").append(tag.getName()).append(">");
    }

    private void declarePrefix(XmlTag tag, String prefix, Map<String, String> scope, StringBuilder sb) {
        if (prefix.isEmpty() || "xmlns".equals(prefix) || scope.containsKey(prefix)) {
            return;
        }

        String namespace = tag.getNamespaceByPrefix(prefix);
        if (namespace.isEmpty()) {
            return;
        }

        scope.put(prefix, namespace);
        sb.append(" xmlns:").append(prefix).append("=\"").append(namespace).append("\"");
    }
}
