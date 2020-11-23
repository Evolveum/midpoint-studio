package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.prism.query.*;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ConnectorXmlSchemaCacheService {

    private static final Logger LOG = Logger.getInstance(ConnectorXmlSchemaCacheService.class);

    private static final Long CACHE_MAX_TTL = 5 * 60 * 1000L; // 5 minute

    private Project project;

    private final ConcurrentHashMap<Key, Value> SCHEMAS = new ConcurrentHashMap();

    public ConnectorXmlSchemaCacheService(Project project) {
        this.project = project;

//        MessageBus bus = project.getMessageBus();
//        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {
//
//            @Override
//            public void environmentChanged(Environment oldEnv, Environment newEnv) {
//                updateCache(newEnv);
//            }
//        });
    }

//    private void updateCache(Environment env) {
//
//    }

    public XmlFile getSchema(String namespace, PsiFile baseFile) {
        if (!(baseFile instanceof XmlFile)) {
            return null;
        }

        if (namespace == null || !namespace.startsWith("http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/bundle/")) {
            return null;
        }

        EnvironmentService environmentService = EnvironmentService.getInstance(project);
        Environment env = environmentService.getSelected();
        if (env == null) {
            return null;
        }

        MidPointClient client = new MidPointClient(project, env, true, true);

        Key key = buildConnectorKey((XmlFile) baseFile, client);
        if (key == null) {
            return null;
        }

        Value value = SCHEMAS.get(key);
        if (value != null && value.getCreated() > System.currentTimeMillis() - CACHE_MAX_TTL) {
            return value.getFile();
        } else {
            SCHEMAS.remove(key);
        }

        RunnableUtils.PluginClassCallable<XmlFile> callable = new RunnableUtils.PluginClassCallable<>() {

            @Override
            public XmlFile callWithPluginClassLoader() throws Exception {
                XmlFile file = getConnectorSchema(namespace, key, client);

//                SCHEMAS.put(key, new Value(file, System.currentTimeMillis()));

                return file;
            }
        };

        callable = new RunnableUtils.PluginClassCallable<XmlFile>() {
            @Override
            public XmlFile callWithPluginClassLoader() throws Exception {
                return (XmlFile) PsiFileFactory.getInstance(project).createFileFromText("connector-123.xsd", XMLLanguage.INSTANCE, "<xml/>");
            }
        };

        try {
            XmlFile file = callable.call();
            return file;
        } catch (Exception ex) {
            LOG.warn("Couldn't prepare connector schema");
        }

        return null;
    }

    public void clear() {
        SCHEMAS.clear();
    }

    private Key buildConnectorKey(XmlFile file, MidPointClient client) {
        QName root = MidPointUtils.createQName(file.getRootTag());
        if (!SchemaConstantsGenerated.C_RESOURCE.equals(root)) {
            // todo improve for c:objects/c:resource
            return null;
        }

        XmlTag connectorRef = MidPointUtils.findSubTag(file.getRootTag(), ResourceType.F_CONNECTOR_REF);
        if (connectorRef == null) {
            return null;
        }

        String oid = connectorRef.getAttributeValue("oid", SchemaConstantsGenerated.NS_COMMON);
        if (oid != null) {
            return new Key(client.getEnvironment(), oid);
        }

        XmlTag filter = MidPointUtils.findSubTag(connectorRef, ObjectReferenceType.F_FILTER);
        String xml = filter.getText();

        ObjectFilter of = null;
        try {
            PrismParser parser = client.createParser(xml);
            SearchFilterType filterType = parser.parseRealValue(SearchFilterType.class);
            of = client.getPrismContext().getQueryConverter().parseFilter(filterType, ConnectorType.class);
        } catch (Exception ex) {
        }

        if (of == null) {
            return null;
        }

        return new Key(client.getEnvironment(), of);
    }

    private XmlFile getConnectorSchema(@NotNull String url, @NotNull Key key, @NotNull MidPointClient client) throws Exception {
        // take resource -> connectorRef or connectorRef/filter, download connector object get schema from there

        MidPointObject object;
        if (key.getOid() != null) {
            object = client.get(ConnectorType.class, key.getOid(), new SearchOptions().raw(true));
        } else {
            PrismContext ctx = client.getPrismContext();
            QueryFactory qf = ctx.queryFactory();

            ObjectPaging paging = qf.createPaging(0, 10, ctx.path(ObjectType.F_NAME), OrderDirection.ASCENDING);

            ObjectQuery query = qf.createQuery(key.getFilter(), paging);

            List<MidPointObject> list = client.search(ConnectorType.class, query, true).getObjects();
            if (list.size() != 1) {
                return null;
            }

            object = list.get(0);
        }

        if (object == null || object.getContent() == null) {
            return null;
        }

        String connector = object.getContent();

        Document doc = DOMUtil.parseDocument(connector);
        Element schema = DOMUtil.getChildElement(doc.getDocumentElement(), ConnectorType.F_SCHEMA.asSingleName());
        if (schema == null) {
            return null;
        }

        Element definition = DOMUtil.getChildElement(schema, XmlSchemaType.F_DEFINITION.asSingleName());
        if (definition == null) {
            return null;
        }

        Element xsdSchema = DOMUtil.getChildElement(definition, DOMUtil.XSD_SCHEMA_ELEMENT);
        if (xsdSchema == null) {
            return null;
        }

        modifyConnectorXsdSchema(xsdSchema);

        String xsd = DOMUtil.serializeDOMToString(xsdSchema);
//        VirtualFile virtualFile = new LightVirtualFile("connector-" + object.getOid() + ".xsd", xsd);

//        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

        return (XmlFile) PsiFileFactory.getInstance(project).createFileFromText("connector-" + object.getOid() + ".xsd", XMLLanguage.INSTANCE, xsd);
//        return (XmlFile) psiFile;
    }

    private void modifyConnectorXsdSchema(Element xsdSchema) {
        if (xsdSchema == null) {
            return;
        }

        List<Element> elements = DOMUtil.getChildElements(xsdSchema, new QName(W3C_XML_SCHEMA_NS_URI, "element"));
        Optional<Element> connectorConfiguration = elements.stream().filter(e -> "connectorConfiguration".equals(DOMUtil.getAttribute(e, "name"))).findFirst();
        connectorConfiguration.ifPresent(e -> xsdSchema.removeChild(e));

        List<Element> complexTypes = DOMUtil.getChildElements(xsdSchema, new QName(W3C_XML_SCHEMA_NS_URI, "complexType"));
        Optional<Element> complexConfigurationType = complexTypes.stream().filter(e -> "ConfigurationType".equals(DOMUtil.getAttribute(e, "name"))).findFirst();
        complexConfigurationType.ifPresent(e -> xsdSchema.removeChild(e));

        Optional<Element> complexConfigurationPropertiesType = complexTypes.stream().filter(e -> "ConfigurationPropertiesType".equals(DOMUtil.getAttribute(e, "name"))).findFirst();
        complexConfigurationPropertiesType.ifPresent(e -> {
            Element sequence = DOMUtil.getChildElement(e, new QName(W3C_XML_SCHEMA_NS_URI, "sequence"));
            if (sequence == null) {
                return;
            }

            List<Element> list = DOMUtil.getChildElements(sequence, new QName(W3C_XML_SCHEMA_NS_URI, "element"));
            if (list == null) {
                return;
            }

            for (Element element : list) {
                Element cloned = (Element) element.cloneNode(true);
                cloned.removeAttribute("minOccurs");
                cloned.removeAttribute("maxOccurs");

                xsdSchema.appendChild(cloned);
            }
        });
    }

    private static class Value {

        private XmlFile file;

        private long created;

        public Value(XmlFile file, long created) {
            this.file = file;
            this.created = created;
        }

        public XmlFile getFile() {
            return file;
        }

        public long getCreated() {
            return created;
        }
    }

    private static class Key {

        private Environment environment;

        private String oid;

        private ObjectFilter filter;

        public Key(Environment environment, String oid) {
            this.environment = environment;
            this.oid = oid;
        }

        public Key(Environment environment, ObjectFilter filter) {
            this.environment = environment;
            this.filter = filter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (environment != null ? !environment.equals(key.environment) : key.environment != null) return false;
            if (oid != null ? !oid.equals(key.oid) : key.oid != null) return false;
            return filter != null ? filter.equals(key.filter) : key.filter == null;
        }

        @Override
        public int hashCode() {
            int result = environment != null ? environment.hashCode() : 0;
            result = 31 * result + (oid != null ? oid.hashCode() : 0);
            result = 31 * result + (filter != null ? filter.hashCode() : 0);
            return result;
        }

        public Environment getEnvironment() {
            return environment;
        }

        public String getOid() {
            return oid;
        }

        public ObjectFilter getFilter() {
            return filter;
        }
    }
}
