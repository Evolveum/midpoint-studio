package com.evolveum.midpoint.studio.impl.lang.converter;

import com.evolveum.midpoint.prism.xnode.*;
import com.intellij.json.psi.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class JsonToXNode implements XNodeConverter {

    @Override
    public @Nullable XNode convertFromPsi(@NotNull PsiElement element) {
        if (element instanceof JsonObject jsonObject) {
            return convertObject(jsonObject);
        } else if (element instanceof JsonArray jsonArray) {
            return convertArray(jsonArray);
        } else if (element instanceof JsonProperty jsonProperty) {
            return convertFromPsi(jsonProperty);
        } else if (element instanceof JsonLiteral jsonLiteral) {
            return convertLiteral(jsonLiteral);
        } else if (element instanceof JsonValue jsonValue) {
            return convertFromPsi(jsonValue);
        }

        return null;
    }

    private MapXNode convertObject(JsonObject jsonObject) {
        MapXNode map = xNodeFactory.map();
        for (JsonProperty property : jsonObject.getPropertyList()) {
            String name = property.getName();

            XNode valueNode = convertFromPsi(property.getValue());
            if (valueNode != null) {
                // FIXME missing put method in XNode interface
//                map.put(new QName(name), valueNode);
            }
        }
        return map;
    }

    private ListXNode convertArray(JsonArray jsonArray) {
        ListXNode list = xNodeFactory.list();
        for (JsonValue value : jsonArray.getValueList()) {
            XNode item = convertFromPsi(value);
            if (item != null) {
//                list.add(item);
            }
        }
        return list;
    }

    private PrimitiveXNode<?> convertLiteral(JsonLiteral literal) {
        Object value = literal.getText();
        return xNodeFactory.primitive(value);
    }

}
