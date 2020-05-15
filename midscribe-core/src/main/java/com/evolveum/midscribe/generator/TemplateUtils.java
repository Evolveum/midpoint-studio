package com.evolveum.midscribe.generator;

import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ConfigurationPropertiesType;
import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ResultsHandlerConfigurationType;
import com.evolveum.midpoint.xml.ns._public.resource.capabilities_3.CapabilityType;
import com.evolveum.midscribe.generator.data.Attribute;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TemplateUtils {

    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    public static String getOrig(PolyStringType poly) {
        return poly != null ? poly.getOrig() : null;
    }

    public static String createHeading(int level) {
        return StringUtils.repeat('=', level);
    }

    public static List<Object> asList(Object... array) {
        return Arrays.asList(array);
    }

    public static String stripIndent(String text) {
        if (text == null) {
            return null;
        }

        String[] lines = text.split("\n", -1);
        int skip = countSkipCharacters(lines);

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String newLine = StringUtils.right(line, line.length() - skip);
            sb.append(newLine).append('\n');
        }

        return sb.toString();
    }

    private static final int countSkipCharacters(String[] lines) {
        int count = 0;

        outer:
        for (String line : lines) {
            if (StringUtils.isEmpty(line)) {
                continue;
            }

            char[] chars = line.toCharArray();
            for (char c : chars) {
                if (Character.isWhitespace(c)) {
                    count++;
                } else {
                    break outer;
                }
            }
        }

        return count;
    }

    public static List<Attribute> getResultsHandlerConfiguration(ResourceType resource) {
        ResultsHandlerConfigurationType config = getConnectorConfiguration(resource, ResultsHandlerConfigurationType.class);
        if (config == null) {
            return new ArrayList<>();
        }

        List<Attribute> attributes = new ArrayList<>();
        addAttribute("enableAttributesToGetSearchResultsHandler", config.isEnableAttributesToGetSearchResultsHandler(), attributes);
        addAttribute("enableCaseInsensitiveFilter", config.isEnableCaseInsensitiveFilter(), attributes);
        addAttribute("enableFilteredResultsHandler", config.isEnableFilteredResultsHandler(), attributes);
        addAttribute("enableNormalizingResultsHandler", config.isEnableNormalizingResultsHandler(), attributes);
        addAttribute("filteredResultsHandlerInValidationMode", config.isFilteredResultsHandlerInValidationMode(), attributes);

        return attributes;
    }

    private static void addAttribute(String name, Object value, List<Attribute> attributes) {
        if (value == null) {
            return;
        }

        attributes.add(new Attribute(name, value));
    }

    public static List<Attribute> getConnectorConfiguration(ResourceType resource) {
        ConfigurationPropertiesType config = getConnectorConfiguration(resource, ConfigurationPropertiesType.class);
        if (config == null) {
            return new ArrayList<>();
        }

        List<Attribute> attributes = new ArrayList<>();

        for (Object obj : config.getAny()) {
            if (!(obj instanceof Element)) {
                continue;
            }

            Element element = (Element) obj;
            String value;
            if (element.getFirstChild() instanceof Text) {
                value = element.getTextContent();
            } else {
                value = "XML"; // todo improve, passwords, etc
            }

            attributes.add(new Attribute(element.getLocalName(), null, value));
        }

        return attributes;
    }

    private static <T> T getConnectorConfiguration(ResourceType resource, Class<T> type) {
        ConnectorConfigurationType config = resource.getConnectorConfiguration();
        List configs = config.getAny();
        if (configs.isEmpty()) {
            return null;
        }

//        Map<String, String> configDescription = getConnectorConfigurationDescription(resource);

        for (Object obj : configs) {
            if (!(obj instanceof JAXBElement)) {
                continue;
            }

            JAXBElement<T> jaxb = (JAXBElement) obj;
            if (jaxb.getValue() == null || !type.isAssignableFrom(jaxb.getValue().getClass())) {
                continue;
            }

            return jaxb.getValue();
        }

        return null;
    }

    private static Map<String, String> getConnectorConfigurationDescription(ResourceType resource) {
        ObjectReferenceType connectorRef = resource.getConnectorRef();
        if (connectorRef == null || connectorRef.getOid() == null) {
            return new HashMap<>();
        }

        Map<String, String> result = new HashMap<>();
//        ConnectorType connector = loadObject(client, connectorRef);
        // todo implement

        return result;
    }

    public static ObjectType loadObject(ObjectReferenceType ref) throws Exception {
//        if (client == null || ref == null) {
//            return null;
//        }


//        ObjectTypes type = ObjectTypes.getObjectTypeFromTypeQName(ref.getType());
//        if (type == null) {
//            type = ObjectTypes.OBJECT;
//        }
//
//        return client.oid(type.getClassDefinition(), ref.getOid()).get();
        return null;
    }

    /**
     * Not very clean way of describing capabilities, but better than nothing
     */
    public static String describeCapability(CapabilityType cap) {
        String description = new ReflectionToStringBuilder(cap, new CustomToStringStyle(), null,
                CapabilityType.class, false, false, true).toString();
        description = description.replaceFirst(" \\+\n   ","");

        return description;
    }

    public static String getNameOfObjectType(String displayName, ShadowKindType kind, String intent) {
        StringBuilder sb = new StringBuilder();
        if (displayName != null) {
            sb.append(displayName);
        }

        if (displayName != null && (kind != null || intent != null)) {
            sb.append(", ");
        }

        if (kind != null) {
            sb.append(kind);
        }

        if (kind != null && intent != null) {
            sb.append("/");
        }

        if (intent != null) {
            sb.append(intent);
        }

        return sb.toString();
    }

    public static String getNameOfObjectType(ObjectSynchronizationType type) {
        if (type == null) {
            return null;
        }

        return getNameOfObjectType(type.getName(), type.getKind(), type.getIntent());
    }

    public static String getNameOfObjectType(ResourceObjectTypeDefinitionType type) {
        if (type == null) {
            return null;
        }

        return getNameOfObjectType(type.getDisplayName(), type.getKind(), type.getIntent());
    }

    public static List<ResourceAttributeDefinitionType> sortAttributes(List<ResourceAttributeDefinitionType> attributes) {
        if (attributes == null) {
            return null;
        }

        List<ResourceAttributeDefinitionType> list = new ArrayList<>();
        list.addAll(attributes);

        Collections.sort(list, (a1, a2) -> String.CASE_INSENSITIVE_ORDER.compare(a1.getRef().toString(), a2.getRef().toString()));

        return list;
    }

    private static class CustomToStringStyle extends ToStringStyle {

        public CustomToStringStyle() {
            setUseClassName(false);
            setUseIdentityHashCode(false);
            setFieldSeparator(" +\n   ");
            setFieldSeparatorAtStart(true);
            setArrayStart("");
            setArrayEnd("");
            setContentStart("");
            setContentEnd("");
        }

        @Override
        protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
            if (value instanceof QName) {
                QName qname = (QName) value;
                buffer.append(qname.getPrefix());
                buffer.append(":");
                buffer.append(qname.getLocalPart());
                return;
            }

            if (value == null || isWrapperType(value.getClass()) || String.class.equals(value.getClass())) {
                super.appendDetail(buffer, fieldName, value);
                return;
            }

            buffer.append(" + \n[");
            buffer.append(value.getClass().getSimpleName()).append(":");
            new ReflectionToStringBuilder(value, new CustomToStringStyle(), buffer,
                    (Class) value.getClass(), false, false, true).toString();
            buffer.append("] +\n");
        }
    }
}
