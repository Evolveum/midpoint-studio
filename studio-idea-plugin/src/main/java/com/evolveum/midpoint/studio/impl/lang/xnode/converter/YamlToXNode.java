package com.evolveum.midpoint.studio.impl.lang.xnode.converter;


import com.evolveum.midpoint.prism.impl.xnode.ListXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.PrimitiveXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.prism.xnode.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.*;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
public class YamlToXNode implements XNodeConverter {

    @Override
    public @Nullable XNode convertFromPsi(@NotNull PsiElement element) {

        if (element instanceof YAMLMapping yamlMapping) {
            return convertMapping(yamlMapping);
        } else if (element instanceof YAMLSequence yamlSequence) {
            return convertSequence(yamlSequence);
        } else if (element instanceof YAMLScalar yamlScalar) {
            return convertScalar(yamlScalar);
        }

        return null;
    }

    private MapXNodeImpl convertMapping(YAMLMapping mapping) {
        MapXNodeImpl map = new MapXNodeImpl();

        for (YAMLKeyValue kv : mapping.getKeyValues()) {
            String key = kv.getKeyText();
            YAMLValue value = kv.getValue();

            if (value != null) {
                XNode valueNode = convertFromPsi(value);
                if (valueNode != null) {
                    map.put(new QName(key), (XNodeImpl) valueNode);
                }
            }
        }

        return map;
    }

    private ListXNodeImpl convertSequence(YAMLSequence sequence) {
        ListXNodeImpl list = new ListXNodeImpl();

        List<YAMLSequenceItem> items = sequence.getItems();
        for (YAMLSequenceItem item : items) {
            YAMLValue value = item.getValue();
            if (value != null) {
                XNode itemNode = convertFromPsi(value);
                if (itemNode != null) {
                    list.add((XNodeImpl) itemNode);
                }
            }
        }

        return list;
    }

    private PrimitiveXNodeImpl<?> convertScalar(YAMLScalar scalar) {
        String textValue = scalar.getTextValue();

        Object value;
        if ("true".equalsIgnoreCase(textValue)) {
            value = true;
        } else if ("false".equalsIgnoreCase(textValue)) {
            value = false;
        } else {
            try {
                value = Integer.parseInt(textValue);
            } catch (NumberFormatException e1) {
                try {
                    value = Double.parseDouble(textValue);
                } catch (NumberFormatException e2) {
                    value = textValue;
                }
            }
        }

        return (PrimitiveXNodeImpl<?>) xNodeFactory.primitive(value);
    }
}
