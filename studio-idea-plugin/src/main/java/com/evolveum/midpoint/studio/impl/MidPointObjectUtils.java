package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.browse.Constants;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointObjectUtils {

    public static final String OBJECTS_XML_PREFIX = "<objects xmlns=\"http://midpoint.evolveum.com/xml/ns/public/common/common-3\">\n";

    public static final String OBJECTS_XML_SUFFIX = "</objects>\n";

    public static final String DELTAS_XML_PREFIX = "<deltaList xsi:type=\"t:ObjectDeltaListType\"" +
            " xmlns:t=\"http://midpoint.evolveum.com/xml/ns/public/common/api-types-3\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";

    public static final String DELTAS_XML_SUFFIX = "</deltas>\n";

    private static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    public static List<MidPointObject> parseText(String text, String notificationKey) {
        return parseText(text, null, notificationKey);
    }

    public static List<MidPointObject> parseProjectFile(VirtualFile file, String notificationKey) {
        try (InputStream is = file.getInputStream()) {
            String text = IOUtils.toString(is, file.getCharset());
            return parseText(text, file, notificationKey);
        } catch (IOException ex) {
            if (notificationKey != null) {
                MidPointUtils.publishExceptionNotification(notificationKey,
                        "Couldn't parse file " + (file != null ? file.getName() : null) + " to DOM", ex);
            }
            return null;
        }
    }

    private static List<MidPointObject> parseText(String text, VirtualFile file, String notificationKey) {
        try {
            Document doc = DOMUtil.parseDocument(text);
            List<MidPointObject> objects = parseDocument(doc, file, file != null ? file.getPath() : null);

            if (objects.size() == 1) {
                objects.get(0).setWholeFile(true);
            }

            return objects;
        } catch (RuntimeException ex) {
            String msg;
            if (file != null) {
                msg = "Couldn't parse file " + file.getName();
            } else {
                msg = "Couldn't parse text '" + StringUtils.abbreviate(text, 10) + "'";
            }

            if (notificationKey != null) {
                MidPointUtils.publishExceptionNotification(notificationKey, msg, ex);
            }

            return new ArrayList<>();
        }

    }

    public static Document parseProjectFileToDOM(VirtualFile file, String notificationKey) {
        try (InputStream is = file.getInputStream()) {
            return DOMUtil.parse(is);
        } catch (IOException ex) {
            if (notificationKey != null) {
                MidPointUtils.publishExceptionNotification(notificationKey,
                        "Couldn't parse file " + (file != null ? file.getName() : null) + " to DOM", ex);
            }

            return null;
        }
    }

    private static List<MidPointObject> parseDocument(Document doc, VirtualFile file, String displayName) {
        List<MidPointObject> rv = new ArrayList<>();
        if (doc == null) {
            return rv;
        }

        Element root = doc.getDocumentElement();
        String localName = root.getLocalName();
        String xsiType = root.getAttributeNS(NS_XSI, "type");
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
        String localName = element.getLocalName();
        boolean executable = Constants.SCRIPTING_ACTIONS.contains(localName);
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
        String xsiType = element.getAttributeNS(NS_XSI, "type");
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
}
