package com.evolveum.midpoint.studio.impl.lang.xnode.converter;

import com.evolveum.midpoint.prism.impl.xnode.ListXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.PrimitiveXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.prism.xnode.*;
import com.intellij.json.psi.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class JsonToXNode implements XNodeConverter {

    @Override
    public @Nullable XNode convertFromPsi(PsiElement element) {

        if (element instanceof JsonObject jsonObject) {
            return convertObject(jsonObject);
        } else if (element instanceof JsonArray jsonArray) {
            return convertArray(jsonArray);
        } else if (element instanceof JsonProperty jsonProperty) {
            return convertProperty(jsonProperty);
        } else if (element instanceof JsonLiteral jsonLiteral) {
            return convertLiteral(jsonLiteral);
        }

        return null;
    }

    private MapXNodeImpl convertObject(JsonObject obj) {
        MapXNodeImpl map = new MapXNodeImpl();

        for (JsonProperty property : obj.getPropertyList()) {
            String key = property.getName();
            PsiElement valueElement = property.getValue();
            if (valueElement != null) {
                XNode valueNode = convertFromPsi(valueElement);

                if (valueNode != null) {
                    map.put(new QName(key), (XNodeImpl) valueNode);
                }
            }
        }

        return map;
    }

    private ListXNodeImpl convertArray(JsonArray array) {
        ListXNodeImpl list = new ListXNodeImpl();
        for (JsonValue value : array.getValueList()) {
            XNode itemNode = convertFromPsi(value);
            if (itemNode != null) {
                list.add((XNodeImpl) itemNode);
            }
        }
        return list;
    }

    public XNode convertProperty(JsonProperty property) {
        String key = property.getName();
        JsonValue value = property.getValue();

        if (value == null) {
            return null;
        }

        XNode valueNode = convertFromPsi(value);
        if (valueNode == null) {
            return null;
        }

        MapXNodeImpl result = new MapXNodeImpl();
        result.put(new QName(key), (XNodeImpl) valueNode);
        return result;
    }

    private PrimitiveXNodeImpl<?> convertLiteral(JsonLiteral literal) {
        return (PrimitiveXNodeImpl<?>) xNodeFactory.primitive(literal.getText());
    }
}
