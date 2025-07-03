package com.evolveum.midpoint.studio.impl.lang.xnode.converter;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.xnode.ListXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeDefinition;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.prism.xnode.*;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.util.exception.SchemaException;
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
    public @Nullable XNode convertFromPsi(PsiElement element) throws SchemaException {
        PrismContext prismContext = StudioPrismContextService.getPrismContext(element.getProject());
        XNodeDefinition schema = XNodeDefinition.root(prismContext.getSchemaRegistry());

        if (element instanceof XmlTag tag) {
            XNode xNode = convertTag(tag);
            xNode.setPosition(calculatePosition(element));
            XNodeDefinition xNodeDefinition = resolveXNodeDefinition(tag.getName(), schema, prismContext.getSchemaRegistry().staticNamespaceContext());
            xNode.setDefinition(xNodeDefinition.itemDefinition());
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
            PrimitiveXNode<?> primitiveXNode = xNodeFactory.primitive(text);
            primitiveXNode.setPosition(calculatePosition(tag));

            return primitiveXNode;
        }

        MapXNodeImpl map = new MapXNodeImpl();

        // Attributes
        for (XmlAttribute attr : attributes) {
            String name = attr.getName();
            String value = attr.getValue();
            if (value != null) {
                QName attrQName = new QName(name);
                PrimitiveXNode<?> primitiveXNode = xNodeFactory.primitive(value);
                primitiveXNode.setPosition(calculatePosition(attr));
                map.put(attrQName, (XNodeImpl) primitiveXNode);
            }
        }

        // If exists subTag and primitive value of tag
        if (!text.isEmpty() && hasChildren) {
            PrimitiveXNode<?> primitiveXNode = xNodeFactory.primitive(text);
            primitiveXNode.setPosition(calculatePosition(tag));
            map.put(new QName(tag.getName()), (XNodeImpl) primitiveXNode);
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
                XNode xNode = convertTag(valueSabTags.get(0));
                xNode.setPosition(calculatePosition(tag.getNavigationElement()));
                map.put(key, (XNodeImpl) xNode);
            } else {
                ListXNodeImpl list = new ListXNodeImpl();
                for (XmlTag subTag : valueSabTags) {
                    XNode xNode = convertTag(subTag);
                    xNode.setPosition(calculatePosition(subTag));
                    list.add((XNodeImpl) convertTag(subTag));
                }

                map.put(key, list);
            }
        }

        return map;
    }
}
