package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.project.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.List;

public class RefGenerator extends Generator {

    private String refName;

    private ObjectTypes applicableFor;

    public RefGenerator(String refName, ObjectTypes applicableFor) {
        this.refName = refName;
        this.applicableFor = applicableFor;
    }

    @Override
    public String getLabel() {
        return "Reference (" + refName + ")";
    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public String generate(Project project, List<ObjectType> objects, GeneratorOptions options) {
        if (objects.isEmpty()) {
            return null;
        }
        Document doc = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "references", "c"));
        Element root = doc.getDocumentElement();
        for (ObjectType object : objects) {
            ObjectTypes type = ObjectTypes.getObjectType(object.getClass());

            if (isApplicableFor(type)) {
                Element refRoot = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, refName, "c"));
                createRefContent(refRoot, object, options);
            } else {
                DOMUtil.createComment(root, " " + getLabel() + " is not applicable for object " + object.getName() + " of type " + type.getTypeQName().getLocalPart() + " ");
            }
        }
        return DOMUtil.serializeDOMToString(doc);
    }

    private boolean isApplicableFor(ObjectTypes type) {
        return MidPointUtils.isAssignableFrom(applicableFor, type);
    }

    @Override
    public boolean supportsSymbolicReferences() {
        return true;
    }

    @Override
    public boolean supportsSymbolicReferencesAtRuntime() {
        return "targetRef".equals(refName);
    }

}
