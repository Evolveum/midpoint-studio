package com.evolveum.midpoint.studio.client;

import com.evolveum.midpoint.common.LocalizationService;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.LocalizableMessage;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ClientUtils {

    public static final List<String> SCRIPTING_ACTIONS = Arrays.asList(
            "executeScript",
            "scriptingExpression",
            "sequence",
            "pipeline",
            "search",
            "filter",
            "select",
            "foreach",
            "action"
    );

    public static final String OBJECTS_XML_PREFIX = "<objects xmlns=\"http://midpoint.evolveum.com/xml/ns/public/common/common-3\">\n";

    public static final String OBJECTS_XML_SUFFIX = "</objects>\n";

    public static final String DELTAS_XML_PREFIX = "<objectDeltaObjectList>";

    public static final String DELTAS_XML_SUFFIX = "</objectDeltaObjectList>\n";

    public static List<MidPointObject> filterObjectTypeOnly(List<MidPointObject> objects) {
        return filterObjectTypeOnly(objects, true);
    }

    public static List<MidPointObject> filterObjectTypeOnly(List<MidPointObject> objects, boolean excludeExecutables) {
        if (objects == null) {
            return null;
        }

        return objects.stream().filter(
                o -> o.getType() != null || (o.isExecutable() && !excludeExecutables)
        ).collect(Collectors.toList());
    }

    public static List<MidPointObject> parseText(String text) {
        return parseText(text, null);
    }

    public static List<MidPointObject> parseProjectFile(File file, Charset charset) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            String text = IOUtils.toString(is, charset);
            return parseText(text, file);
        }
    }

    private static List<MidPointObject> parseText(String text, File file) {
        Document doc = DOMUtil.parseDocument(text);
        String displayName = file != null ? file.getPath() : null;

        List<MidPointObject> objects = parseDocument(doc, file, displayName);

        if (objects.size() == 1) {
            objects.get(0).setWholeFile(true);
        }

        return objects;
    }

    private static List<MidPointObject> parseDocument(Document doc, File file, String displayName) {
        List<MidPointObject> rv = new ArrayList<>();
        if (doc == null) {
            return rv;
        }

        Element root = doc.getDocumentElement();
        String localName = root.getLocalName();
        String xsiType = root.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type");
        if ("actions".equals(localName) || "objects".equals(localName) || (xsiType != null && xsiType.contains("ObjectListType"))) {
            for (Element child : DOMUtil.listChildElements(root)) {
                DOMUtil.fixNamespaceDeclarations(child);
                MidPointObject o = parseElement(child);
                if (o != null) {
                    o.setRoot(false);
                    rv.add(o);
                }
            }
        } else {
            MidPointObject o = parseElement(root);
            if (o != null) {
                o.setRoot(true);
                rv.add(o);
            }
        }

        for (int i = 0; i < rv.size(); i++) {
            MidPointObject o = rv.get(i);
            o.setObjectIndex(i);
            o.setFile(file);
            String name;
            if (displayName != null) {
                name = displayName;
            } else if (file != null) {
                name = file.getPath();
            } else {
                name = "(unknown source)";
            }
            if (rv.size() > 1) {
                name += " (object " + (i + 1) + " of " + rv.size() + ")";
            }
            o.setDisplayName(name);
        }

        if (rv.size() > 0) {
            rv.get(rv.size() - 1).setLast(true);
        }

        return rv;
    }

    private static MidPointObject parseElement(Element element) {
        String namespace = element.getNamespaceURI();
        String localName = element.getLocalName();

        boolean executable = (namespace == null || SchemaConstantsGenerated.NS_SCRIPTING.equals(namespace)) && SCRIPTING_ACTIONS.contains(localName);
        ObjectTypes type = getObjectType(element);

        MidPointObject o = new MidPointObject(DOMUtil.serializeDOMToString(element), type, executable);
        String oid = element.getAttribute("oid");
        if (StringUtils.isNotBlank(oid)) {
            o.setOid(oid);
        }
        Element nameElement = DOMUtil.getChildElement(element, "name");
        if (nameElement != null) {
            o.setName(nameElement.getTextContent());
        }
        return o;
    }

    private static ObjectTypes getObjectType(Element element) {
        String xsiType = element.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type");
        if (xsiType != null) {
            xsiType = xsiType.replaceFirst("^.*:", "");

            for (ObjectTypes t : ObjectTypes.values()) {
                if (xsiType.equals(t.getTypeQName().getLocalPart())) {
                    return t;
                }
            }
        }

        String localName = element.getLocalName();
        if (localName == null) {
            return null;
        }

        for (ObjectTypes t : ObjectTypes.values()) {
            if (localName.equals(t.getElementName().getLocalPart())) {
                return t;
            }
        }

        return null;
    }

    public static PrismSerializer<String> getSerializer(PrismContext prismContext) {
        return prismContext.xmlSerializer()
                .options(SerializationOptions.createSerializeReferenceNames());
    }

    public static PrismParser createParser(PrismContext ctx, InputStream data) {
        ParsingContext parsingContext = ctx.createParsingContextForCompatibilityMode();
        return ctx.parserFor(data).language(PrismContext.LANG_XML).context(parsingContext);
    }

    public static PrismParser createParser(PrismContext ctx, String xml) {
        ParsingContext parsingContext = ctx.createParsingContextForCompatibilityMode();
        return ctx.parserFor(xml).language(PrismContext.LANG_XML).context(parsingContext);
    }

    public static String serialize(PrismContext prismContext, Object object) throws SchemaException {
        final QName fakeQName = new QName(PrismConstants.NS_TYPES, "object");

        PrismSerializer<String> serializer = getSerializer(prismContext);

        String result;
        if (object instanceof ObjectType || object instanceof PrismObject) {
            PrismObject o = object instanceof PrismObject ? (PrismObject) object : ((ObjectType) object).asPrismObject();
            ObjectTypes type = ObjectTypes.getObjectType(o.getCompileTimeClass());
            result = serializer.serialize(new JAXBElement(type.getElementName(), o.getClass(), o.asObjectable()));
        } else if (object instanceof OperationResult) {
            LocalizationService localizationService = new LocalizationServiceImpl();
            Function<LocalizableMessage, String> resolveKeys = msg -> localizationService.translate(msg, Locale.US);
            OperationResultType operationResultType = ((OperationResult) object).createOperationResultType(resolveKeys);
            result = serializer.serializeAnyData(operationResultType, fakeQName);
        } else {
            result = serializer.serializeAnyData(object, fakeQName);
        }

        return result;
    }
}
