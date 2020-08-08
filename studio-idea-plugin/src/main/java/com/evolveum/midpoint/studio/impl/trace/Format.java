package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.function.Function;

public enum Format {

    AUTO("Auto", obj -> formatAutomatically(simplify(obj))),

    DEBUG_DUMP("Debug dump", obj -> formatDebugDumpable(simplify(obj))),

    XML("XML", obj -> formatValue(simplify(obj), PrismContext.LANG_XML)),

    JSON("JSON", obj -> formatValue(simplify(obj), PrismContext.LANG_JSON)),

    YAML("YAML", obj -> formatValue(simplify(obj), PrismContext.LANG_YAML)),

    TO_STRING("toString", obj -> String.valueOf(simplify(obj)));

    private final String displayName;

    private final Function<Object, String> formatter;

    Format(String displayName, Function<Object, String> formatter) {
        this.displayName = displayName;
        this.formatter = formatter;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String[] getDisplayNames() {
        return Arrays.stream(values()).map(v -> v.displayName).toArray(String[]::new);
    }

    public String format(Object object) {
        return object != null ? formatter.apply(object) : "";
    }

    private static String formatAutomatically(Object obj) {
        String first = formatDebugDumpable(obj);
        if (!first.contains("com.evolveum.midpoint.xml.ns._public")) {
            return first;
        } else {
            return formatValue(obj, PrismContext.LANG_YAML);
        }
    }

    private static String formatDebugDumpable(Object obj) {
        return obj instanceof DebugDumpable ? ((DebugDumpable) obj).debugDump() : obj.toString();
    }

    private static String formatValue(Object obj, String language) {
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Number) {
            return String.valueOf(obj);
        }
        try {
            PrismContext prismContext = MidPointUtils.DEFAULT_PRISM_CONTEXT; // todo fix
            if (obj instanceof Item) {
                return prismContext.serializerFor(language).serialize((Item<?, ?>) obj);
            } else if (obj instanceof PrismValue) {
                return prismContext.serializerFor(language).serialize((PrismValue) obj, SchemaConstants.C_VALUE);
            } else {
                Class<?> clazz = obj.getClass();
                if (Containerable.class.isAssignableFrom(clazz) ||
                        RawType.class.equals(clazz) ||
                        clazz.getAnnotation(XmlType.class) != null ||
                        XsdTypeMapper.getTypeFromClass(clazz) != null) {
                    return prismContext.serializerFor(language).serializeRealValue(obj, SchemaConstants.C_VALUE);
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
