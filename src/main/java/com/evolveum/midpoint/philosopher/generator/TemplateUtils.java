package com.evolveum.midpoint.philosopher.generator;

import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ConfigurationPropertiesType;
import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ResultsHandlerConfigurationType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TemplateUtils {

    public static String getOrig(PolyStringType poly) {
        if (poly == null) {
            return null;
        }
        for (Object content : poly.getContent()) {
            if (content instanceof String) {
                return (String) content;
            }
            // TODO: DOM elements and JAXB elements
        }
        return null;
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

    private static Map<String, String> getConnectorConfigurationDescription(Service client, ResourceType resource) {
        ObjectReferenceType connectorRef = resource.getConnectorRef();
        if (connectorRef == null || connectorRef.getOid() == null) {
            return new HashMap<>();
        }

        Map<String, String> result = new HashMap<>();
//        ConnectorType connector = loadObject(client, connectorRef);
        // todo implement

        return result;
    }

    public static ObjectType loadObject(Service client, ObjectReferenceType ref) throws Exception {
        if (client == null || ref == null) {
            return null;
        }


//        ObjectTypes type = ObjectTypes.getObjectTypeFromTypeQName(ref.getType());
//        if (type == null) {
//            type = ObjectTypes.OBJECT;
//        }
//
//        return client.oid(type.getClassDefinition(), ref.getOid()).get();
        return null;
    }

    public static class Attribute {

        private String name;
        private String description;
        private Object value;

        public Attribute(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public Attribute(String name, String description, Object value) {
            this.name = name;
            this.description = description;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Object getValue() {
            return value;
        }
    }
}
