package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.XmlSchemaType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.stream.Collectors;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * TODO caching
 * <p>
 * Created by Viliam Repan (lazyman).
 */
public class ConnectorXmlSchemaProvider extends XmlSchemaProvider {

    private static final Logger LOG = Logger.getInstance(ConnectorXmlSchemaProvider.class);

    private final Set<String> UNKNOWN = new HashSet<>();

    private final Map<String, XmlFile> SCHEMAS = new HashMap<>();

    @Override
    public boolean isAvailable(@NotNull XmlFile file) {
        if (file == null || file.getRootTag() == null) {
            return false;
        }

        return SchemaConstantsGenerated.NS_COMMON.equals(file.getRootTag().getNamespace());
    }

    @Nullable
    @Override
    public XmlFile getSchema(@NotNull String url, @Nullable Module module, @NotNull PsiFile baseFile) {
        if (url == null) {
            return null;
        }

        return getSchema(url, baseFile);
    }

    private synchronized XmlFile getSchema(String url, PsiFile baseFile) {
        if (url == null) {
            return null;
        }

        if (UNKNOWN.contains(url)) {
            return null;
        }

        XmlFile schema = SCHEMAS.get(url);
        if (schema != null) {
            return schema;
        }

        if (!url.startsWith("http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/bundle/")) {
            return null;
        }

        Project project = baseFile.getProject();
        EnvironmentService environmentService = EnvironmentService.getInstance(project);
        Environment env = environmentService.getSelected();
        if (env == null) {
            return null;
        }

        RunnableUtils.PluginClassCallable<XmlFile> callable = new RunnableUtils.PluginClassCallable<>() {

            @Override
            public XmlFile callWithPluginClassLoader() throws Exception {
                return getConnectorSchema(url, baseFile, env);
            }
        };

        try {
            return callable.call();
        } catch (Exception ex) {
            LOG.warn("Couldn't prepare connector schema");
        }

        return null;
    }

    private XmlFile getConnectorSchema(@NotNull String url, @NotNull PsiFile baseFile, @NotNull Environment environment) throws Exception {
        // take resource -> connectorRef or connectorRef/filter, download connector object get schema from there

        List<MidPointObject> objects = MidPointObjectUtils.parseProjectFile(baseFile.getVirtualFile(), null);
        List<MidPointObject> resources = new ArrayList<>();
        if (objects != null) {
            resources = objects.stream().filter(o -> ObjectTypes.RESOURCE.equals(o.getType())).collect(Collectors.toList());
        }

        if (resources.size() != 1) {
            // we don't know which resource we should process
            return null;
        }

        MidPointObject object = resources.get(0);

        Project project = baseFile.getProject();

        MidPointClient client = new MidPointClient(project, environment, true);
        PrismObject<ResourceType> resource = (PrismObject) client.parseObject(object.getContent());

        ResourceType resourceType = resource.asObjectable();
        ObjectReferenceType connectorRef = resourceType.getConnectorRef();
        if (connectorRef == null) {
            return null;
        }

        if (connectorRef.getOid() != null) {
            String connector = client.getRaw(ConnectorType.class, connectorRef.getOid(), new SearchOptions().raw(true));
            if (connector == null) {
                // todo cache "no result"
                return null;
            }

            Document doc = DOMUtil.parseDocument(connector);
            Element schema = DOMUtil.getChildElement(doc.getDocumentElement(), ConnectorType.F_SCHEMA.asSingleName());
            if (schema == null) {
                // todo cache "no result"
                return null;
            }

            Element definition = DOMUtil.getChildElement(schema, XmlSchemaType.F_DEFINITION.asSingleName());
            if (definition == null) {
                // todo cache "no result"
                return null;
            }

            Element xsdSchema = DOMUtil.getChildElement(definition, DOMUtil.XSD_SCHEMA_ELEMENT);
            if (xsdSchema == null) {
                // todo cache "no result"
                return null;
            }

            modifyConnectorXsdSchema(xsdSchema);

            String xsd = DOMUtil.serializeDOMToString(xsdSchema);
            VirtualFile virtualFile = new LightVirtualFile("connector-schema.xsd", xsd);

            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            XmlFile file = (XmlFile) psiFile;

            SCHEMAS.put(url, file);
        }

        return null;
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

    private static class SchemaKey {

        String connectorName;

        String connectorVersion;

        String namespace;

        public SchemaKey(String connectorName, String connectorVersion, String namespace) {
            this.connectorName = connectorName;
            this.connectorVersion = connectorVersion;
            this.namespace = namespace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SchemaKey schemaKey = (SchemaKey) o;

            if (connectorName != null ? !connectorName.equals(schemaKey.connectorName) : schemaKey.connectorName != null)
                return false;
            if (connectorVersion != null ? !connectorVersion.equals(schemaKey.connectorVersion) : schemaKey.connectorVersion != null)
                return false;
            return namespace != null ? namespace.equals(schemaKey.namespace) : schemaKey.namespace == null;
        }

        @Override
        public int hashCode() {
            int result = connectorName != null ? connectorName.hashCode() : 0;
            result = 31 * result + (connectorVersion != null ? connectorVersion.hashCode() : 0);
            result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
            return result;
        }
    }
}
