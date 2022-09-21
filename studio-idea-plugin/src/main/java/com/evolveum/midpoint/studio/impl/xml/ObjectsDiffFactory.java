package com.evolveum.midpoint.studio.impl.xml;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectsDiffFactory {

    public static final String NS_STUDIO = "http://midpoint.evolveum.com/xml/ns/public/common/studio-3";

    public static final QName Q_DIFF_LIST = new QName(NS_STUDIO, "diffList");

    public static final QName Q_DIFF = new QName(NS_STUDIO, "diff");

    public static final QName Q_FIRST_OBJECT = new QName(NS_STUDIO, "firstObject");

    public static final QName Q_SECOND_OBJECT = new QName(NS_STUDIO, "secondObject");

    public static final QName Q_LOCATION = new QName(NS_STUDIO, "location");

    public static final QName Q_FILE_NAME = new QName(NS_STUDIO, "fileName");

    public static final QName Q_OBJECT = new QName(NS_STUDIO, "object");

    private PrismContext prismContext;

    public ObjectsDiffFactory(@NotNull PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    public DiffType parseObjectsDiff(String xml) throws SchemaException, IOException {
        if (xml == null) {
            return null;
        }

        Document doc = ClientUtils.parseDocument(xml);
        return parseObjectsDiff(doc.getDocumentElement());
    }

    public DiffType parseObjectsDiff(Element element) throws SchemaException, IOException {
        if (element == null) {
            return null;
        }

        QName name = DOMUtil.getQName(element);
        if (!Q_DIFF.equals(name)) {
            return null;
        }

        DiffType objectsDiff = new DiffType();

        Element first = DOMUtil.getChildElement(element, Q_FIRST_OBJECT);
        objectsDiff.setFirstObject(parseDiffObject(first));

        Element second = DOMUtil.getChildElement(element, Q_SECOND_OBJECT);
        objectsDiff.setSecondObject(parseDiffObject(second));

        return objectsDiff;
    }

    private DiffObjectType parseDiffObject(Element element) throws SchemaException, IOException {
        if (element == null) {
            return null;
        }

        DiffObjectType diffObject = new DiffObjectType();
        diffObject.setFileName(parseStringElement(DOMUtil.getChildElement(element, Q_FILE_NAME)));
        diffObject.setLocation(parseLocation(DOMUtil.getChildElement(element, Q_LOCATION)));
        diffObject.setObject(parseObject(DOMUtil.getChildElement(element, Q_OBJECT)));

        return diffObject;
    }

    private ObjectType parseObject(Element element) throws SchemaException, IOException {
        if (element == null) {
            return null;
        }

        PrismObject object = ClientUtils.createParser(prismContext, DOMUtil.serializeDOMToString(element)).parse();
        return (ObjectType) object.asObjectable();
    }

    private LocationType parseLocation(Element element) {
        String str = parseStringElement(element);

        return LocationType.fromValue(str);
    }

    private String parseStringElement(Element element) {
        if (element == null) {
            return null;
        }

        return element.getTextContent();
    }

    public String serializeObjectsDiffToString(DiffType objectsDiff) throws SchemaException {
        Element element = serializeObjectsDiffToDom(objectsDiff);
        if (element == null) {
            return null;
        }

        return DOMUtil.serializeDOMToString(element);
    }

    public Element serializeObjectsDiffToDom(DiffType objectsDiff) throws SchemaException {
        if (objectsDiff == null) {
            return null;
        }

        Document doc = DOMUtil.getDocument();
        Element root = DOMUtil.createElement(doc, Q_DIFF);
        doc.appendChild(root);

        addElement(root, serializeDiffObject(doc, Q_FIRST_OBJECT, objectsDiff.getFirstObject()));
        addElement(root, serializeDiffObject(doc, Q_SECOND_OBJECT, objectsDiff.getSecondObject()));

        return doc.getDocumentElement();
    }

    private Element serializeDiffObject(Document doc, QName name, DiffObjectType diffObject) throws SchemaException {
        Element element = DOMUtil.createElement(doc, name);
        addElement(element, serializeSimple(doc, Q_FILE_NAME, () -> diffObject.getFileName()));
        addElement(element, serializeLocation(doc, diffObject.getLocation()));
        addElement(element, serializeObject(doc, diffObject.getObject()));

        return element;
    }

    private void addElement(Element parent, Element child) {
        if (child != null) {
            parent.appendChild(child);
        }
    }

    private Element serializeSimple(Document doc, QName name, Supplier value) {
        Object val = value.get();
        if (val == null) {
            return null;
        }

        Element element = DOMUtil.createElement(doc, name);
        element.setTextContent(val.toString());

        return element;
    }

    private Element serializeObject(Document doc, ObjectType object) throws SchemaException {
        if (object == null) {
            return null;
        }

        String xml = ClientUtils.getSerializer(prismContext).serializeRealValue(object, Q_OBJECT);
        Document d = ClientUtils.parseDocument(xml);

        Element element = d.getDocumentElement();
        doc.adoptNode(element);

        return element;
    }

    private Element serializeLocation(Document doc, LocationType location) {
        if (location == null) {
            return null;
        }

        Element element = DOMUtil.createElement(doc, Q_LOCATION);
        element.setTextContent(location.getValue());

        return element;
    }
}
