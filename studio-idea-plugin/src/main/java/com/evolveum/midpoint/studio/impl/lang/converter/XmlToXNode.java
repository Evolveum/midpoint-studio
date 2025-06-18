package com.evolveum.midpoint.studio.impl.lang.converter;

import com.evolveum.midpoint.prism.impl.xnode.ListXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.prism.xnode.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.*;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by Dominik.
 */
public class XmlToXNode implements XNodeConverter {

    @Override
    public @Nullable XNode convertFromPsi(PsiElement element) {

        if (element instanceof XmlTag tag) {
            return xNodeFactory.map(new QName(tag.getName()), convertTag(tag));
        }

        return null;
    }

    public XNode convertTag(XmlTag tag) {
        XmlTag[] children = tag.getSubTags();
        XmlAttribute[] attributes = tag.getAttributes();
        String text = tag.getValue().getTrimmedText();

        boolean hasChildren = children.length > 0;
        boolean hasAttributes = attributes.length > 0;

        if (!hasChildren && !hasAttributes) {
            return xNodeFactory.primitive(text);
        }

        MapXNodeImpl map = new MapXNodeImpl();

        // Attributes
        for (XmlAttribute attr : attributes) {
            String name = attr.getName();
            String value = attr.getValue();
            if (value != null) {
                QName attrQName = new QName(name);
                map.put(attrQName, (XNodeImpl) xNodeFactory.primitive(value));
            }
        }

        // If exists subTag and primitive value of tag
        if (!text.isEmpty() && hasChildren) {
            map.put(new QName(tag.getName()), (XNodeImpl) xNodeFactory.primitive(text));
        }

        // Children
        Map<String, List<XmlTag>> grouped = new LinkedHashMap<>();
        for (XmlTag child : children) {
            grouped.computeIfAbsent(child.getName(), k -> new ArrayList<>()).add(child);
        }

        for (Map.Entry<String, List<XmlTag>> entry : grouped.entrySet()) {
            QName key = new QName(entry.getKey());
            List<XmlTag> valueSabTags = entry.getValue();

            if (valueSabTags.size() == 1) {
                map.put(key, (XNodeImpl) convertTag(valueSabTags.get(0)));
            } else {
                ListXNodeImpl list = new ListXNodeImpl();

                for (XmlTag subTag : valueSabTags) {
                    list.add((XNodeImpl) convertTag(subTag));
                }

                map.put(key, list);
            }
        }

        return map;
    }
}
