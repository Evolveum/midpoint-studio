package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public abstract class Generator {

    public static final Generator NULL_GENERATOR = new NullGenerator();

    public abstract String getLabel();

    public abstract String generate(List<ObjectType> objects, GeneratorOptions options);

    public boolean supportsRawOption() {
        return false;
    }

    public boolean supportsDryRunOption() {
        return false;
    }

    public boolean supportsSymbolicReferences() {
        return false;
    }

    public boolean supportsSymbolicReferencesAtRuntime() {
        return false;
    }

    public boolean isExecutable() {
        return false;
    }

    public void createRefContent(Element refRoot, ObjectType object, GeneratorOptions options) {
        createRefContent(refRoot, object, options, getSymbolicRefItemName(object), getSymbolicRefItemValue(object));
    }

    public static void createRefContent(Element refRoot, ObjectType object, GeneratorOptions options, String symbolicRefItemName, String symbolicRefItemValue) {
        DOMUtil.setQNameAttribute(refRoot, "type", MidPointUtils.getTypeQName(object));
        if (options.isSymbolicReferences()) {
            Element filter = DOMUtil.createSubElement(refRoot, new QName(Constants.COMMON_NS, "filter", "c"));
            Element equal = DOMUtil.createSubElement(filter, new QName(Constants.QUERY_NS, "equal", "q"));
            DOMUtil.createSubElement(equal, new QName(Constants.QUERY_NS, "path", "q")).setTextContent(symbolicRefItemName);
            DOMUtil.createSubElement(equal, new QName(Constants.QUERY_NS, "value", "q")).setTextContent(symbolicRefItemValue);
            if (options.isSymbolicReferencesRuntime()) {
                DOMUtil.createSubElement(refRoot, new QName(Constants.COMMON_NS, "resolutionTime", "c")).setTextContent("run");
            }
        } else {
            refRoot.setAttribute("oid", object.getOid());
            DOMUtil.createComment(refRoot, " " + object.getName() + " ");
        }
    }

    protected String getSymbolicRefItemValue(ObjectType object) {
        return object.getName().getOrig();
    }

    protected String getSymbolicRefItemName(ObjectType object) {
        return "name";
    }

    protected void createInOidQueryFilter(Element filter, List<ObjectType> objects) {
        Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID);
        for (ObjectType o : objects) {
            DOMUtil.createSubElement(inOid, Constants.Q_VALUE).setTextContent(o.getOid());
        }
    }

    public boolean supportsWrapIntoTask() {
        return false;
    }

    public boolean supportsCreateSuspended() {
        return false;
    }

    protected List<Batch> createBatches(List<ObjectType> objects, GeneratorOptions options, ObjectTypes applicableTo) {
        List<Batch> rv = new ArrayList<>();

        if (options.isBatchByOids()) {
            Batch current = null;
            int index = 0;
            for (ObjectType object : objects) {
                if (!MidPointUtils.isAssignableFrom(applicableTo, ObjectTypes.getObjectType(object.getClass()))) {
                    continue;
                }
                if (current == null || current.getObjects().size() == options.getBatchSize()) {
                    current = new Batch();
                    current.setFirst(index);
                    rv.add(current);
                }
                current.getObjects().add(object);
                index++;
            }
        } else {
            rv.add(new Batch());
        }
        return rv;
    }

    protected boolean requiresExecutionConfirmation() {
        return false;
    }

    public String getActionDescription() {
        return null;
    }
}
