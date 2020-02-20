package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public abstract class Generator {

    public static final Generator NULL_GENERATOR = new NullGenerator();

    public abstract String getLabel();

    public abstract String generate(List<MidPointObject> objects, GeneratorOptions options);

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

    public void createRefContent(Element refRoot, MidPointObject object, GeneratorOptions options) {
        createRefContent(refRoot, object, options, getSymbolicRefItemName(object), getSymbolicRefItemValue(object));
    }

    public static void createRefContent(Element refRoot, MidPointObject object, GeneratorOptions options, String symbolicRefItemName, String symbolicRefItemValue) {
        DOMUtil.setQNameAttribute(refRoot, "type", object.getType().getTypeQName());
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

    protected String getSymbolicRefItemValue(MidPointObject object) {
        return object.getName();
    }

    protected String getSymbolicRefItemName(MidPointObject object) {
        return "name";
    }

    protected void createInOidQueryFilter(Element filter, List<MidPointObject> objects) {
        Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID);
        for (MidPointObject o : objects) {
            DOMUtil.createSubElement(inOid, Constants.Q_VALUE).setTextContent(o.getOid());
        }
    }

    public boolean supportsWrapIntoTask() {
        return false;
    }

    public boolean supportsCreateSuspended() {
        return false;
    }

    protected List<Batch> createBatches(List<MidPointObject> objects, GeneratorOptions options, ObjectTypes applicableTo) {
        List<Batch> rv = new ArrayList<Batch>();

        if (options.isBatchByOids()) {
            Batch current = null;
            int index = 0;
            for (MidPointObject object : objects) {
                if (!MidPointUtils.isAssignableFrom(applicableTo, object.getType())) {
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
