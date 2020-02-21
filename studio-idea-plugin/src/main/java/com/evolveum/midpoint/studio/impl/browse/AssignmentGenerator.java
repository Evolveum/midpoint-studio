package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.List;

public class AssignmentGenerator extends Generator {

    @Override
    public String getLabel() {
        return "Assignment";
    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public String generate(List<ObjectType> objects, GeneratorOptions options) {
        if (objects.isEmpty()) {
            return null;
        }
        Document doc = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "assignments", "c"));
        Element root = doc.getDocumentElement();
        for (ObjectType object : objects) {
            ObjectTypes type = ObjectTypes.getObjectType(object.getClass());

            if (isApplicableFor(type)) {
                Element aRoot = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, "assignment", "c"));
                if (type == ObjectTypes.RESOURCE) {
                    Element construction = DOMUtil.createSubElement(aRoot, new QName(Constants.COMMON_NS, "construction", "c"));
                    Element resourceRef = DOMUtil.createSubElement(construction, new QName(Constants.COMMON_NS, "resourceRef", "c"));
                    createRefContent(resourceRef, object, options);
                } else {
                    Element targetRef = DOMUtil.createSubElement(aRoot, new QName(Constants.COMMON_NS, "targetRef", "c"));
                    createRefContent(targetRef, object, options);
                }
            } else {
                DOMUtil.createComment(root, " " + getLabel() + " cannot be created for object " + object.getName() + " of type " + MidPointUtils.getTypeQName(object).getLocalPart() + " ");
            }
        }
        return DOMUtil.serializeDOMToString(doc);
    }

    private boolean isApplicableFor(ObjectTypes type) {
        return MidPointUtils.isAssignableFrom(ObjectTypes.ABSTRACT_ROLE, type) || type == ObjectTypes.RESOURCE;
    }

    public boolean supportsSymbolicReferences() {
        return true;
    }

    public boolean supportsSymbolicReferencesAtRuntime() {
        return true;
    }
}
