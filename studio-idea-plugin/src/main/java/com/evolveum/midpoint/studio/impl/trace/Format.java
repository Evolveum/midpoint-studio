package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import org.w3c.dom.*;

import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.function.BiFunction;

public enum Format {

    AUTO("Auto", (obj, ctx) -> formatAutomatically(simplify(obj), ctx)),

    DEBUG_DUMP("Debug dump", (obj, ctx) -> formatDebugDumpable(simplify(obj))),

    XML("XML", (obj, ctx) -> formatValue(simplify(obj), PrismContext.LANG_XML, ctx)),

    XML_SIMPLIFIED("XML simplified", (obj, ctx) -> formatValueXmlSimplified(simplify(obj), ctx)),

    JSON("JSON", (obj, ctx) -> formatValue(simplify(obj), PrismContext.LANG_JSON, ctx)),

    YAML("YAML", (obj, ctx) -> formatValue(simplify(obj), PrismContext.LANG_YAML, ctx)),

    TO_STRING("toString", (obj, ctx) -> String.valueOf(simplify(obj)));

    private final String displayName;

    private final BiFunction<Object, FormattingContext, String> formatter;

    Format(String displayName, BiFunction<Object, FormattingContext, String> formatter) {
        this.displayName = displayName;
        this.formatter = formatter;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String[] getDisplayNames() {
        return Arrays.stream(values()).map(v -> v.displayName).toArray(String[]::new);
    }

    public String format(Object object, FormattingContext ctx) {
        return object != null ? formatter.apply(object, ctx) : "";
    }

    private static String formatAutomatically(Object obj, FormattingContext ctx) {
        String first = formatDebugDumpable(obj);
        if (!first.contains("com.evolveum.midpoint.xml.ns._public")) {
            return first;
        } else {
            return formatValue(obj, PrismContext.LANG_YAML, ctx);
        }
    }

    private static String formatDebugDumpable(Object obj) {
        return obj instanceof DebugDumpable ? ((DebugDumpable) obj).debugDump() : obj.toString();
    }

    private static String formatValue(Object obj, String language, FormattingContext ctx) {
        return String.valueOf(formatValueInternal(obj, language, false, ctx));
    }

    private static String formatValueXmlSimplified(Object obj, FormattingContext ctx) {
        Object formatted = formatValueInternal(obj, PrismContext.LANG_XML, true, ctx);
        if (formatted instanceof Element) {
            return serializeXmlSimplified((Element) formatted);
        } else {
            return String.valueOf(formatted);
        }
    }

    @Experimental
    private static String serializeXmlSimplified(Element root) {
        deleteXsiTypeDeclarations(root);
        renameNodesToNoNamespace(root);
        deleteNamespaceDeclarations(root);
        return DOMUtil.serializeDOMToString(root);
    }

    private static void renameNodesToNoNamespace(Node node) {
        Document document = node.getOwnerDocument();
        if (node instanceof Element || node instanceof Attr) {
            document.renameNode(node, null, node.getLocalName());
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            renameNodesToNoNamespace(list.item(i));
        }
    }

    private static void deleteXsiTypeDeclarations(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = attributes.getLength()-1; i >= 0; i--) {
            Node node = attributes.item(i);
            if (node instanceof Attr) {
                Attr attribute = (Attr) node;
                if (attribute.getName().equals("xsi:type")) {
                    attributes.removeNamedItem(attribute.getName());
                }
            }
        }
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            if (child instanceof Element) {
                deleteXsiTypeDeclarations((Element) child);
            }
        }
    }

    private static void deleteNamespaceDeclarations(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = attributes.getLength()-1; i >= 0; i--) {
            Node node = attributes.item(i);
            if (node instanceof Attr) {
                Attr attribute = (Attr) node;
                if (attribute.getName().startsWith("xmlns")) {
                    attributes.removeNamedItem(attribute.getName());
                }
            }
        }
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            if (child instanceof Element) {
                deleteNamespaceDeclarations((Element) child);
            }
        }
    }

    private static Object formatValueInternal(Object obj, String language, boolean dom, FormattingContext ctx) {
        if (obj instanceof String) {
            return obj;
        } else if (obj instanceof Number) {
            return String.valueOf(obj);
        }
        try {
            PrismContext prismContext = MidPointUtils.DEFAULT_PRISM_CONTEXT; // todo fix
            PrismSerializer<?> serializer = dom ? prismContext.domSerializer() : prismContext.serializerFor(language);
            if (obj instanceof Item) {
                Item<?, ?> item = (Item<?, ?>) obj;
                ctx.addObjectNames(item);
                return serializer.serialize(item);
            } else if (obj instanceof PrismValue) {
                PrismValue value = (PrismValue) obj;
                ctx.addObjectNames(value);
                return serializer.serialize(value, SchemaConstants.C_VALUE);
            } else {
                Class<?> clazz = obj.getClass();
                if (Containerable.class.isAssignableFrom(clazz) ||
                        RawType.class.equals(clazz) ||
                        clazz.getAnnotation(XmlType.class) != null ||
                        XsdTypeMapper.getTypeFromClass(clazz) != null) {
                    ctx.addObjectNames(obj);
                    return serializer.serializeRealValue(obj, SchemaConstants.C_VALUE);
                } else {
                    return String.valueOf(obj);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return "Cannot serialize " + obj + ":\n" + t.getMessage();
        }
    }

    private static Object simplify(Object obj) {
        if (obj instanceof OpNode) {
            OperationResultType clone = ((OpNode) obj).getResult().clone();
            clone.getPartialResults().clear();
            clone.getTrace().clear();
            return clone;
        } else if (obj instanceof PrismPropertyValue) {
            PrismPropertyValue<?> ppv = (PrismPropertyValue<?>) obj;
            if (!ppv.hasValueMetadata()) {
                return ppv.getRealValue();
            } else {
                return ppv;
            }
        } else if (obj instanceof PrismProperty && ((PrismProperty<?>) obj).size() == 1) {
            return simplify(((PrismProperty<?>) obj).getValues().get(0));
        } else {
            return obj;
        }
    }

}
