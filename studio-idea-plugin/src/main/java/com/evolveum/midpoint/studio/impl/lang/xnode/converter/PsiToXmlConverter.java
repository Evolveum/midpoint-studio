package com.evolveum.midpoint.studio.impl.lang.xnode.converter;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.*;
import org.jetbrains.annotations.Nullable;

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
        sb.append("<").append(tag.getName());

        for (XmlAttribute attr : tag.getAttributes()) {
            sb.append(" ")
                .append(attr.getName())
                .append("=\"")
                .append(attr.getValue())
                .append("\"");
        }

        XmlTag[] children = tag.getSubTags();
        String textValue = tag.getValue().getTrimmedText();

        if (deep) {
            if (children.length == 0 && textValue.isEmpty()) {
                sb.append("/>");
                return;
            }

            sb.append(">");

            if (children.length == 0) {
                sb.append(textValue);
            } else {
                for (XmlTag child : children) {
                    buildXml(child, sb, deep);
                }
            }
        } else {
            if (children.length == 0 && textValue.isEmpty()) {
                sb.append("/>");
                return;
            }

            sb.append(">");
        }

        sb.append("</").append(tag.getName()).append(">");
    }
}
