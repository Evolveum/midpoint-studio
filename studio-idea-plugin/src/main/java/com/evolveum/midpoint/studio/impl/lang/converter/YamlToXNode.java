package com.evolveum.midpoint.studio.impl.lang.converter;


import com.evolveum.midpoint.prism.xnode.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.*;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class YamlToXNode implements XNodeConverter {

    @Override
    public @Nullable XNode convertFromPsi(@NotNull PsiElement element) {

        if (element instanceof YAMLDocument yamlDocument) {
            YAMLValue value = yamlDocument.getTopLevelValue();
            return convertFromPsi(value);
        } else if (element instanceof YAMLKeyValue yamlKeyValue) {
            return convertFromPsi(yamlKeyValue.getValue());
        } else if (element instanceof YAMLMapping yamlMapping) {
            return convertMapping(yamlMapping);
        } else if (element instanceof YAMLSequence) {
            return convertSequence((YAMLSequence) element);
        } else if (element instanceof YAMLScalar) {
            return convertScalar((YAMLScalar) element);
        } else if (element instanceof YAMLValue) {
            return convertFromPsi(element);
        }

        return null;
    }


    private MapXNode convertMapping(YAMLMapping mapping) {
        MapXNode map = xNodeFactory.map();
        for (YAMLKeyValue pair : mapping.getKeyValues()) {
            if (pair != null) {
                // FIXME missing put method in XNode interface
//                String name = pair.getKeyText();
//                map.put(new QName(name), pair.getValue());
            }
        }
        return map;
    }

    private ListXNode convertSequence(YAMLSequence sequence) {
        ListXNode list = xNodeFactory.list();
        for (YAMLSequenceItem item : sequence.getItems()) {
            XNode x = convertFromPsi(item);
            if (x != null) {
                // FIXME
//                list.add(x);
            }
        }
        return list;
    }

    private PrimitiveXNode<?> convertScalar(YAMLScalar scalar) {
        String text = scalar.getTextValue();
        Object value = parseScalarValue(text);
        return xNodeFactory.primitive(value);
    }

    private Object parseScalarValue(String text) {

        if ("true".equalsIgnoreCase(text)) return true;
        if ("false".equalsIgnoreCase(text)) return false;

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e1) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e2) {
                return text;
            }
        }
    }
}
