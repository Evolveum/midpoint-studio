package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.List;

public class ShowGenerator extends Generator {

    public ShowGenerator() {
    }

    @Override
    public String getLabel() {
        return null;        // not needed
    }

    @Override
    public String generate(List<MidPointObject> objects, GeneratorOptions options) {
        if (objects.isEmpty()) {
            return null;
        }
        if (objects.size() == 1) {
            return objects.get(0).getXml();
        }

        try {
            Document doc = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "objects", "c"));
            Element root = doc.getDocumentElement();
            for (MidPointObject object : objects) {
                Element obj = DOMUtil.parseDocument(object.getXml()).getDocumentElement();
                root.appendChild(doc.importNode(obj, true));
            }
            return DOMUtil.serializeDOMToString(root);
        } catch (Throwable t) {
        	// todo print error to console or something
//            Console.logError("Couldn't copy selected objects to new XML document", t);
            return null;
        }
    }

}
