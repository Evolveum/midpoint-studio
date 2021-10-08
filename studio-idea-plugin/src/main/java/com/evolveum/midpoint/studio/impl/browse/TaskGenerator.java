package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemObjectsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskRecurrenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.List;

public class TaskGenerator extends Generator {

    private static final String URI_PREFIX_SYNC = "http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task";
    private static final String URI_PREFIX_MODEL = "http://midpoint.evolveum.com/xml/ns/public/model";

    public enum Action {
        RECOMPUTE("recompute", URI_PREFIX_SYNC + "/recompute/handler-3", ObjectTypes.FOCUS_TYPE, "Recomputation", false),
        DELETE("delete", URI_PREFIX_SYNC + "/delete/handler-3", ObjectTypes.OBJECT, "Utility", true),
        MODIFY("modify (execute changes)", URI_PREFIX_SYNC + "/execute/handler-3", ObjectTypes.OBJECT, "ExecuteChanges", false),
        SHADOW_CHECK("check shadow integrity", URI_PREFIX_MODEL + "/shadow-integrity-check/handler-3", ObjectTypes.SHADOW, "Utility", true);

        private final String displayName, handlerUri, category;
        private final ObjectTypes applicableTo;
        private final boolean requiresConfirmation;

        private Action(String displayName, String handlerUri, ObjectTypes applicableTo, String category, boolean requiresConfirmation) {
            this.displayName = displayName;
            this.handlerUri = handlerUri;
            this.applicableTo = applicableTo;
            this.category = category;
            this.requiresConfirmation = requiresConfirmation;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getHandlerUri() {
            return handlerUri;
        }
    }

    private Action action;

    public TaskGenerator(Action action) {
        this.action = action;
    }

    @Override
    public String getLabel() {
        return "Native task: " + action.displayName;
    }

    @Override
    public String generate(List<ObjectType> objects, GeneratorOptions options) {
        // TODO deduplicate with bulk actions
        ObjectTypes type = action.applicableTo;
        if (options.isBatchUsingOriginalQuery()) {
            if (options.getOriginalQueryTypes().size() == 1) {
                ObjectTypes selected = options.getOriginalQueryTypes().iterator().next();
                if (MidPointUtils.isAssignableFrom(type, selected)) {
                    type = selected;
                }
            }
        }

        List<Batch> batches = createBatches(objects, options, action.applicableTo);

        Element root = null;
        if (batches.size() > 1) {
            root = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "objects")).getDocumentElement();
            addStandardNamespaceDefinitions(root);
        }

        for (Batch batch : batches) {
            Element task;

            if (batches.size() == 1) {
                task = DOMUtil.getDocument(SchemaConstantsGenerated.C_TASK).getDocumentElement();
                addStandardNamespaceDefinitions(task);

                root = task;
            } else {
                task = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, "object"));
                task.setAttribute("xsi:type", "c:TaskType");
            }

            DOMUtil.createSubElement(task, TaskType.F_NAME)
                    .setTextContent("Execute " + action.getDisplayName() + " on objects " + (batch.getFirst() + 1) + " to " + (batch.getLast() + 1));

            Element extension = DOMUtil.createSubElement(task, TaskType.F_EXTENSION);
            DOMUtil.createSubElement(extension, new QName(Constants.MEXT_NS, "objectType", "mext")).setTextContent(type.getTypeQName().getLocalPart());
            Element objectQuery = DOMUtil.createSubElement(extension, new QName(Constants.MEXT_NS, "objectQuery", "mext"));
            if (options.isBatchByOids()) {
                Element filter = DOMUtil.createSubElement(objectQuery, Constants.Q_FILTER_PREFIXED);
                Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_PREFIXED);
                for (ObjectType o : batch.getObjects()) {
                    DOMUtil.createSubElement(inOid, Constants.Q_VALUE_PREFIXED).setTextContent(o.getOid());
                    DOMUtil.createComment(inOid, " " + o.getName() + " ");
                }
            } else {
                try {
                    Element originalQuery = DOMUtil.parseDocument(options.getOriginalQuery()).getDocumentElement();
                    List<Element> children = DOMUtil.listChildElements(originalQuery);
                    for (Element child : children) {
                        DOMUtil.fixNamespaceDeclarations(child);
                        objectQuery.appendChild(root.getOwnerDocument().adoptNode(child));
                    }
                } catch (RuntimeException e) {
                    MidPointUtils.publishExceptionNotification(null, TaskGenerator.class,
                            GeneratorAction.NOTIFICATION_KEY, "Couldn't parse XML query", e);
                    throw e;
                }
            }

            ObjectTypes superType = null;
            if (options.isBatchByOids()) {
                for (ObjectType o : batch.getObjects()) {
                    superType = MidPointUtils.commonSuperType(superType, ObjectTypes.getObjectType(o.getClass()));
                }
            } else {
                for (ObjectTypes t : options.getOriginalQueryTypes()) {
                    superType = MidPointUtils.commonSuperType(superType, t);
                }
            }
            if (superType == null) {
                superType = ObjectTypes.OBJECT;
            }

            if (action == Action.MODIFY) {
                Element delta = DOMUtil.createSubElement(extension, new QName(Constants.MEXT_NS, "objectDelta", "mext"));
                DOMUtil.createSubElement(delta, new QName(Constants.TYPES_NS, "changeType", "t")).setTextContent("modify");
                DOMUtil.createSubElement(delta, new QName(Constants.TYPES_NS, "objectType", "t")).setTextContent(superType.getTypeQName().getLocalPart());
                DOMUtil.createSubElement(delta, new QName(Constants.TYPES_NS, "oid", "t")).setTextContent("unused");
                Element itemDelta = DOMUtil.createSubElement(delta, new QName(Constants.TYPES_NS, "itemDelta", "t"));
                DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "modificationType", "t")).setTextContent("add");
                DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "path", "t")).setTextContent("TODO (e.g. displayName)");
                Element value = DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "value", "t"));
                DOMUtil.setXsiType(value, DOMUtil.XSD_STRING);
                value.setTextContent("TODO");
            } else if (action == Action.SHADOW_CHECK) {
                DOMUtil.createComment(extension, " <mext:fix>normalization</mext:fix> ");
                DOMUtil.createComment(extension, " <mext:fix>uniqueness</mext:fix> ");
                DOMUtil.createComment(extension, " <mext:fix>intents</mext:fix> ");
                DOMUtil.createComment(extension, " <mext:fix>extraData</mext:fix> ");
                DOMUtil.createComment(extension, " <mext:diagnose>owners</mext:diagnose> ");
                DOMUtil.createComment(extension, " <mext:diagnose>fetch</mext:diagnose> ");
            }

            DOMUtil.createSubElement(task, TaskType.F_OWNER_REF).setAttribute("oid", SystemObjectsType.USER_ADMINISTRATOR.value());
            DOMUtil.createSubElement(task, new QName(SchemaConstantsGenerated.NS_COMMON, "executionStatus")).setTextContent(
                    options.isCreateSuspended() ? "suspended" : "runnable"
            );
            DOMUtil.createSubElement(task, TaskType.F_CATEGORY).setTextContent(action.category);
            DOMUtil.createSubElement(task, TaskType.F_HANDLER_URI).setTextContent(action.handlerUri);
            DOMUtil.createSubElement(task, TaskType.F_RECURRENCE).setTextContent(TaskRecurrenceType.SINGLE.value());
        }

        return DOMUtil.serializeDOMToString(root);
    }

    @Override
    public boolean supportsRawOption() {
        return true;
    }

    @Override
    public boolean supportsDryRunOption() {
        return false;
    }

    @Override
    public boolean isExecutable() {
        return action != Action.MODIFY;
    }

    @Override
    protected boolean requiresExecutionConfirmation() {
        return action.requiresConfirmation;
    }

    public String getActionDescription() {
        return action.displayName;
    }
}
