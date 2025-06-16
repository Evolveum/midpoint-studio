package com.evolveum.midpoint.studio.impl.lang.converter;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.impl.xnode.XNodeDefinition;
import com.evolveum.midpoint.prism.impl.xnode.XNodeFactoryImpl;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.PrimitiveXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.prism.xnode.XNodeFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Arrays;

/**
 * Created by Dominik.
 */
public class XmlToXNode implements XNodeConverter {
    @Override
    public @Nullable XNode convertFromPsi(@NotNull PsiElement element) {

        if (element instanceof XmlTag tag) {

            QName qName = new QName(tag.getName());
            XNode processingNode = xNodeFactory.primitive(qName);



//            for (XmlTag child : children) {
//                processTag(child, current);
//            }

            return xNodeFactory.map(new QName(tag.getNamespace(), tag.getName()), xNodeFactory.primitive(tag.getValue()));
        }

        return null;
    }

    private void processTag(XmlTag tag, MapXNode parent) {
        QName tagName = new QName(tag.getNamespace(), tag.getName());
        MapXNode current = xNodeFactory.map();

//         FIXME add attr to xnode
//        for (XmlAttribute attr : tag.getAttributes()) {
//            String name = attr.getName();
//            String value = attr.getValue();
//            if (value != null) {
//                PrimitiveXNode<?> attrNode = xNodeFactory.primitive(value);
//                QName attrName   = new QName("@" + name);
////                current.put(attrName, attrNode);
//            }
//        }

        String text = tag.getValue().getTrimmedText();

        if (!text.isEmpty()) {
            PrimitiveXNode<?> valueNode = xNodeFactory.primitive(text);

        }

        for (XmlTag child : tag.getSubTags()) {
            processTag(child, current);
        }
    }

}
