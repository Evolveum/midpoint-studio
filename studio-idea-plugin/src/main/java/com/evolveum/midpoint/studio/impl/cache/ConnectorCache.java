package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.impl.match.MatchingRuleRegistryFactory;
import com.evolveum.midpoint.prism.impl.xml.GlobalDynamicNamespacePrefixMapper;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.XmlSchemaType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.*;
import java.util.stream.Collectors;

public class ConnectorCache extends ObjectCache<ConnectorType> {

    private static final Logger LOG = Logger.getInstance(ConnectorCache.class);

    private static final String ICF_NS_PREFIX = "http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/bundle/";

    private static final MatchingRuleRegistry MATCHING_REGISTRY = MatchingRuleRegistryFactory.createRegistry();

    private record CacheValue(ConnectorType connector, XmlFile icfConnectorSchema, XmlFile connectorSchema) {

    }

    private final Map<String, CacheValue> schemaCache = new HashMap<>();

    public ConnectorCache(@NotNull Project project) {
        super(project, ConnectorType.class);
    }

    @Override
    void clear() {
        schemaCache.clear();

        super.clear();
    }

    @Override
    protected void cacheObjects(Collection<MidPointObject> objects) {
        schemaCache.clear();

        super.cacheObjects(objects);

        for (ConnectorType connector : list()) {
            GlobalDynamicNamespacePrefixMapper.registerPrefixGlobal(
                    connector.getNamespace(), SchemaConstants.CONNECTOR_CONFIGURATION_PREFIX);

            try {
                String xml = ClientUtils.serialize(PrismContext.get(), connector);
                schemaCache.put(connector.getOid(), new CacheValue(connector, buildIcfSchema(connector.getOid(), xml), buildConnectorSchema(connector.getOid(), xml)));
            } catch (SchemaException ex) {
                LOG.error("Couldn't serialize connector to xml", ex);
            }
        }
    }

    private XmlFile buildIcfSchema(String oid, String connector) {
        Element xsdSchema = getXsdSchema(connector);

        List<Element> complexTypes = DOMUtil.getChildElements(xsdSchema, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "complexType"));
        Optional<Element> complexConfigurationPropertiesType = complexTypes.stream().filter(e -> "ConfigurationPropertiesType".equals(DOMUtil.getAttribute(e, "name"))).findFirst();

        String importNamespace = xsdSchema.getAttribute("targetNamespace");

        Document doc = DOMUtil.getDocument(DOMUtil.XSD_SCHEMA_ELEMENT);
        Element schema = doc.getDocumentElement();
        schema.setAttribute("xmlns:t", SchemaConstantsGenerated.NS_TYPES);
        schema.setAttribute("xmlns:a", SchemaConstantsGenerated.NS_ANNOTATION);
        schema.setAttribute("xmlns:icfc", SchemaConstantsGenerated.NS_ICF_CONFIGURATION);
        schema.setAttribute("xmlns:con", importNamespace);
        schema.setAttribute("targetNamespace", SchemaConstantsGenerated.NS_ICF_CONFIGURATION);
        schema.setAttribute("elementFormDefault", "qualified");

        Element _import = DOMUtil.createElement(doc, xsdElement("import"));
        _import.setAttribute("namespace", importNamespace);
        schema.appendChild(_import);

        Element element = DOMUtil.createElement(doc, xsdElement("element"));
        element.setAttribute("name", "configurationProperties");
        element.setAttribute("type", "icfc:ConfigurationPropertiesType");
        schema.appendChild(element);

        Element complex = DOMUtil.createElement(doc, xsdElement("complexType"));
        complex.setAttribute("name", "ConfigurationPropertiesType");
        schema.appendChild(complex);

        Element sequence = DOMUtil.createElement(doc, xsdElement("sequence"));
        complex.appendChild(sequence);

        complexConfigurationPropertiesType.ifPresent(e -> {
            Element seq = DOMUtil.getChildElement(e, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "sequence"));
            if (seq == null) {
                return;
            }

            List<Element> list = DOMUtil.getChildElements(seq, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "element"));

            for (Element el : list) {
                Element cloned = (Element) el.cloneNode(true);
                cloned.setAttribute("ref", "con:" + cloned.getAttribute("name"));

                cloned.removeAttribute("name");
                cloned.removeAttribute("type");

                sequence.appendChild(doc.adoptNode(cloned));
            }
        });

        String xsd = DOMUtil.serializeDOMToString(doc);

        return createXmlFile("connector-" + oid + "-schema.xsd", xsd);
    }

    private XmlFile createXmlFile(String name, String content) {
        return ApplicationManager.getApplication()
                .runReadAction(
                        (Computable<? extends XmlFile>)
                                () -> (XmlFile) PsiFileFactory.getInstance(getProject())
                                        .createFileFromText(name, XMLLanguage.INSTANCE, content));
    }

    private QName xsdElement(String name) {
        return new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, name, "xsd");
    }

    private XmlFile buildConnectorSchema(String oid, String connector) {
        Element xsdSchema = getXsdSchema(connector);

        if (StringUtils.isBlank(xsdSchema.getAttribute("xmlns:t"))) {
            xsdSchema.setAttribute("xmlns:t", SchemaConstantsGenerated.NS_TYPES);
        }

        List<Element> elements = DOMUtil.getChildElements(xsdSchema, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "element"));
        Optional<Element> connectorConfiguration = elements.stream().filter(e -> "connectorConfiguration".equals(DOMUtil.getAttribute(e, "name"))).findFirst();
        connectorConfiguration.ifPresent(e -> xsdSchema.removeChild(e));

        List<Element> complexTypes = DOMUtil.getChildElements(xsdSchema, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "complexType"));
        Optional<Element> complexConfigurationType = complexTypes.stream().filter(e -> "ConfigurationType".equals(DOMUtil.getAttribute(e, "name"))).findFirst();
        complexConfigurationType.ifPresent(e -> xsdSchema.removeChild(e));

        Optional<Element> complexConfigurationPropertiesType = complexTypes.stream().filter(e -> "ConfigurationPropertiesType".equals(DOMUtil.getAttribute(e, "name"))).findFirst();
        complexConfigurationPropertiesType.ifPresent(e -> {
            Element sequence = DOMUtil.getChildElement(e, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "sequence"));
            if (sequence == null) {
                return;
            }

            List<Element> list = DOMUtil.getChildElements(sequence, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "element"));

            for (Element element : list) {
                Element cloned = (Element) element.cloneNode(true);
                cloned.removeAttribute("minOccurs");
                cloned.removeAttribute("maxOccurs");

                xsdSchema.appendChild(cloned);
            }
        });
        complexConfigurationPropertiesType.ifPresent(e -> xsdSchema.removeChild(e));

        String xsd = DOMUtil.serializeDOMToString(xsdSchema);

        return createXmlFile("connector-" + oid + "-schema-modified.xsd", xsd);
    }

    private Element getXsdSchema(String connector) {
        Document doc = DOMUtil.parseDocument(connector);
        Element schema = DOMUtil.getChildElement(doc.getDocumentElement(), ConnectorType.F_SCHEMA.asSingleName());
        if (schema == null) {
            return null;
        }

        Element definition = DOMUtil.getChildElement(schema, XmlSchemaType.F_DEFINITION.asSingleName());
        if (definition == null) {
            return null;
        }

        return DOMUtil.getChildElement(definition, DOMUtil.XSD_SCHEMA_ELEMENT);
    }

    private XmlFile getSchema(String url, String connectorOid) {
        List<CacheValue> values = schemaCache.values().stream().filter(c -> connectorOid.equals(c.connector.getOid())).collect(Collectors.toList());
        if (values.size() != 1) {
            return null;
        }

        return getSchema(url, values.get(0));
    }

    public XmlFile getSchema(String url, XmlFile file) {
        if (url == null || (!SchemaConstantsGenerated.NS_ICF_CONFIGURATION.equals(url)
                && !url.startsWith(ICF_NS_PREFIX))) {
            return null;
        }

        XmlTag rootTag = file.getRootTag();
        QName root = MidPointUtils.createQName(rootTag);

        if (DOMUtil.XSD_SCHEMA_ELEMENT.equals(root)) {
            String fileName = file.getVirtualFile().getName();
            String uuid = fileName.replaceFirst("^connector-", "").replaceFirst("-schema.xsd$", "");

            if (MidPointUtils.UUID_PATTERN.matcher(uuid).matches()) {
                return getSchema(url, uuid);
            }
        }

        if (SchemaConstants.C_OBJECTS.equals(root)) {
            XmlTag[] tags = rootTag.getSubTags();
            if (tags.length > 0) {
                rootTag = tags[0];
                root = MidPointUtils.createQName(rootTag);
            }
        }

        if (!SchemaConstantsGenerated.C_RESOURCE.equals(root)
                && !isResourceUsingXsi(rootTag)) {
            return null;
        }

        XmlTag connectorRef = MidPointUtils.findSubTag(rootTag, ResourceType.F_CONNECTOR_REF);
        if (connectorRef == null) {
            return null;
        }

        String oid = connectorRef.getAttributeValue("oid", SchemaConstantsGenerated.NS_COMMON);
        if (oid != null) {
            CacheValue value = schemaCache.get(oid);
            if (value == null) {
                return null;
            }
            return getSchema(url, value);
        }

        XmlTag filter = MidPointUtils.findSubTag(connectorRef, ObjectReferenceType.F_FILTER);

        RunnableUtils.PluginClassCallable<XmlFile> callable = new RunnableUtils.PluginClassCallable<>() {

            @Override
            public XmlFile callWithPluginClassLoader() throws Exception {
                ObjectFilter of = null;
                try {
                    String xml = updateNamespaces(filter);

                    PrismContext ctx = MidPointUtils.DEFAULT_PRISM_CONTEXT;
                    PrismParser parser = ClientUtils.createParser(ctx, xml);

                    SearchFilterType filterType = parser.parseRealValue(SearchFilterType.class);
                    of = ctx.getQueryConverter().parseFilter(filterType, ConnectorType.class);
                } catch (Exception ex) {
                    LOG.debug("Couldn't parse connectorRef filter defined in resource, reason: " + ex.getMessage() + "(" + ex.getClass().getName() + ")");
                }

                if (of == null) {
                    return null;
                }

                // todo check if this for-cycle returns more than one result -> more connector matches filter, throw some error
                for (CacheValue cacheValue : schemaCache.values()) {
                    try {
                        boolean match = ObjectQuery.match(cacheValue.connector(), of, MATCHING_REGISTRY);
                        if (!match) {
                            continue;
                        }
                    } catch (SchemaException ex) {
                        LOG.error("Couldn't match connector with connectorRef filter defined in resource", ex);
                    }

                    return getSchema(url, cacheValue);
                }

                return null;
            }
        };

        try {
            return callable.call();
        } catch (Exception ex) {
            if (!(ex instanceof ControlFlowException)) {
                LOG.error("Couldn't find connector schema", ex);
            }
        }

        return null;
    }

    private boolean isResourceUsingXsi(XmlTag tag) {
        XmlAttribute attr = tag.getAttribute("type", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        if (attr == null || StringUtils.isEmpty(attr.getValue())) {
            return false;
        }

        String value = attr.getValue();
        String[] array = value.split(":", -1);

        String local;
        if (array.length == 2) {
            String ns = tag.getNamespaceByPrefix(array[0]);
            local = array[1];
            if (!ResourceType.COMPLEX_TYPE.getNamespaceURI().equals(ns)) {
                return false;
            }
        } else {
            local = array[0];
        }

        return ResourceType.COMPLEX_TYPE.getLocalPart().equals(local);
    }

    private String updateNamespaces(XmlTag filter) {
        XmlTag copy = (XmlTag) filter.copy();

        XmlTag tag = filter;
        while (tag != null) {
            for (Map.Entry<String, String> entry : tag.getLocalNamespaceDeclarations().entrySet()) {
                if (copy.getLocalNamespaceDeclarations().containsKey(entry.getKey())) {
                    continue;
                }

                String name = "xmlns";
                if (StringUtils.isNotEmpty(entry.getKey())) {
                    name += ":";
                }
                name += entry.getKey();

                copy.setAttribute(name, entry.getValue());
            }

            tag = tag.getParentTag();
        }

        return copy.getText();
    }

    private XmlFile getSchema(String url, CacheValue value) {
        if (url == null || value == null) {
            return null;
        }

        if (SchemaConstantsGenerated.NS_ICF_CONFIGURATION.equals(url)) {
            return value.icfConnectorSchema;
        }

        return value.connectorSchema;
    }
}
