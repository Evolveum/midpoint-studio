package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.intellij.notification.NotificationType;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.List;

public class BulkActionGenerator extends Generator {

    public enum Action {

        RECOMPUTE("recompute", "recompute", ObjectTypes.FOCUS_TYPE, false, true, false),

        ENABLE("enable", "enable", ObjectTypes.FOCUS_TYPE, false, true, false),

        DISABLE("disable", "disable", ObjectTypes.FOCUS_TYPE, false, true, false),

        DELETE("delete", "delete", ObjectTypes.OBJECT, true, true, true),

        MODIFY("modify", "modify", ObjectTypes.OBJECT, true, true, false),

        ASSIGN_THIS("assign selected objects (to something)", "modify", ObjectTypes.ABSTRACT_ROLE, true, true, false),

        ASSIGN_TO_THIS("assign (something) to selected objects", "modify", ObjectTypes.FOCUS_TYPE, true, true, false),

        LOG("log", "log", ObjectTypes.OBJECT, false, false, false),

        TEST_RESOURCE("test resource", "test-resource", ObjectTypes.RESOURCE, false, false, false),

        VALIDATE("validate resource", "validate", ObjectTypes.RESOURCE, false, false, false),

        EXECUTE_SCRIPT("execute script", "execute-script", ObjectTypes.OBJECT, false, false, false),

        NOTIFY("send notifications", "notify", ObjectTypes.OBJECT, false, false, false);

        private final String displayName, actionName;
        private final ObjectTypes applicableTo;
        private final boolean supportsRaw, supportsDryRun, requiresConfirmation;

        Action(String displayName, String actionName, ObjectTypes applicableTo, boolean supportsRaw, boolean supportsDryRun, boolean requiresConfirmation) {
            this.displayName = displayName;
            this.actionName = actionName;
            this.applicableTo = applicableTo;
            this.supportsRaw = supportsRaw;
            this.supportsDryRun = supportsDryRun;
            this.requiresConfirmation = requiresConfirmation;
        }
    }

    private Action action;

    public BulkActionGenerator(Action action) {
        this.action = action;
    }

    @Override
    public String getLabel() {
        return "Bulk action: " + action.displayName;
    }

    @Override
    public String generate(List<ObjectType> objects, GeneratorOptions options) {
        Element top = null;

        List<Batch> batches;
        if (action != Action.ASSIGN_THIS) {
            batches = createBatches(objects, options, action.applicableTo);
        } else {
            // very special case: we assign to (yet) unspecified single object
            if (!options.isBatchByOids()) {
                MidPointUtils.publishNotification(GeneratorAction.NOTIFICATION_KEY, "Not supported",
                        "Using original query is not supported for this action.", NotificationType.ERROR);
                return null;
            }
            Batch batch = new Batch();
            batch.getObjects().add(new UserType());
            batches = Collections.singletonList(batch);
        }
        Element root;
        if (batches.size() > 1) {
            top = root = DOMUtil.getDocument(new QName(Constants.COMMON_NS, options.isWrapActions() ? "objects" : "actions", "c")).getDocumentElement();
        } else {
            root = null;
        }

        for (Batch batch : batches) {
            Element batchRoot;
            Element task = null;
            if (options.isWrapActions()) {
                if (root == null) {
                    task = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "task", "c")).getDocumentElement();
                    top = task;
                } else {
                    task = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, "task", "c"));
                }
                DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "name", "c")).setTextContent("Execute " + action.displayName + " on objects " + (batch.getFirst() + 1) + " to " + (batch.getLast() + 1));
                Element extension = DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "extension", "c"));
                Element executeScript = DOMUtil.createSubElement(extension, new QName(Constants.SCEXT_NS, "executeScript", "scext"));
                batchRoot = executeScript;
            } else {
                batchRoot = root;
            }

            Element pipe;
            if (batchRoot == null) {
                pipe = DOMUtil.getDocument(new QName(Constants.SCRIPT_NS, "pipeline", "s")).getDocumentElement();
                top = pipe;
            } else {
                pipe = DOMUtil.createSubElement(batchRoot, new QName(Constants.SCRIPT_NS, "pipeline", "s"));
            }

            createSearch(pipe, options, batch);
            createAction(pipe, options, objects);

            if (task != null) {
                DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "taskIdentifier", "c")).setTextContent(generateTaskIdentifier());
                DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "ownerRef", "c")).setAttribute("oid", "00000000-0000-0000-0000-000000000002");
                DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "executionStatus", "c")).setTextContent(
                        options.isCreateSuspended() ? "suspended" : "runnable"
                );
                DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "category", "c")).setTextContent("BulkActions");
                DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "handlerUri", "c")).setTextContent("http://midpoint.evolveum.com/xml/ns/public/model/scripting/handler-3");
                DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "recurrence", "c")).setTextContent("single");
            }
        }

        return DOMUtil.serializeDOMToString(top);
    }

    public static String generateTaskIdentifier() {
        return System.currentTimeMillis() + ":" + Math.round(Math.random() * 1000000000.0);
    }

    public void createSearch(Element root, GeneratorOptions options, Batch batch) {

        ObjectTypes type = action.applicableTo;
        if (options.isBatchUsingOriginalQuery()) {
            if (options.getOriginalQueryTypes().size() == 1) {
                ObjectTypes selected = options.getOriginalQueryTypes().iterator().next();
                if (MidPointUtils.isAssignableFrom(type, selected)) {
                    type = selected;
                }
            }
        }

        Element search = DOMUtil.createSubElement(root, new QName(Constants.SCRIPT_NS, "expression", "s"));
        DOMUtil.setXsiType(search, new QName(Constants.SCRIPT_NS, "SearchExpressionType", "s"));
        DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent(type.getTypeQName().getLocalPart());

        if (options.isBatchByOids()) {
            Element filter = DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "searchFilter", "s"));
            Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_Q);
            for (ObjectType o : batch.getObjects()) {
                DOMUtil.createSubElement(inOid, Constants.Q_VALUE_Q).setTextContent(o.getOid());
                DOMUtil.createComment(inOid, " " + o.getName() + " ");
            }
        } else {
            try {
                Element originalQuery = DOMUtil.parseDocument(options.getOriginalQuery()).getDocumentElement();
                Element originalFilter = DOMUtil.getChildElement(originalQuery, "filter");
                if (originalFilter != null) {
                    Element filter = DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "searchFilter", "s"));
                    List<Element> children = DOMUtil.listChildElements(originalFilter);
                    for (Element child : children) {
                        DOMUtil.fixNamespaceDeclarations(child);
                        filter.appendChild(root.getOwnerDocument().adoptNode(child));
                    }
                }
            } catch (RuntimeException e) {
                MidPointUtils.publishExceptionNotification(GeneratorAction.NOTIFICATION_KEY, "Couldn't parse XML query", e);
            }
        }
    }

    public void createSingleSourceSearch(Element root, PrismObject object) {
        Element search = DOMUtil.createSubElement(root, new QName(Constants.SCRIPT_NS, "expression", "s"));
        DOMUtil.setXsiType(search, new QName(Constants.SCRIPT_NS, "SearchExpressionType", "s"));
        DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent(MidPointUtils.getTypeQName(object).getLocalPart());
        Element filter = DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "searchFilter", "s"));
        if (object.getOid() != null) {
            Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_Q);
            DOMUtil.createSubElement(inOid, Constants.Q_VALUE_Q).setTextContent(object.getOid());
            DOMUtil.createComment(inOid, " " + object.getName() + " ");
        } else if (object.getName() != null) {
            Element equal = DOMUtil.createSubElement(filter, Constants.Q_EQUAL_Q);
            DOMUtil.createSubElement(equal, Constants.Q_PATH_Q).setTextContent("name");
            DOMUtil.createSubElement(equal, Constants.Q_VALUE_Q).setTextContent(object.getName().getOrig());
        } else {
            MidPointUtils.publishNotification(GeneratorAction.NOTIFICATION_KEY, "Warning",
                    "No OID nor name provided; action on this object cannot be executed.", NotificationType.WARNING);

            Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_Q);
            DOMUtil.createSubElement(inOid, Constants.Q_VALUE_Q).setTextContent("no such object 919432948jkas");
        }
    }

    // objects should be non-null for ASSIGN_THIS action
    public void createAction(Element root, GeneratorOptions options, List<ObjectType> objects) {
        Element actionE = DOMUtil.createSubElement(root, new QName(Constants.SCRIPT_NS, "expression", "s"));
        DOMUtil.setXsiType(actionE, new QName(Constants.SCRIPT_NS, "ActionExpressionType", "s"));
        DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent(action.actionName);
        if (options.isRaw() && supportsRawOption()) {
            Element rawParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
            DOMUtil.createSubElement(rawParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("raw");
            DOMUtil.createSubElement(rawParam, new QName(Constants.COMMON_NS, "value", "c")).setTextContent("true");
        }
        if (options.isDryRun() && supportsDryRunOption()) {
            Element rawParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
            DOMUtil.createSubElement(rawParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("dryRun");
            DOMUtil.createSubElement(rawParam, new QName(Constants.COMMON_NS, "value", "c")).setTextContent("true");
        }

        if (action == Action.MODIFY) {
            Element deltaParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
            DOMUtil.createSubElement(deltaParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("delta");
            Element objectDelta = DOMUtil.createSubElement(deltaParam, new QName(Constants.COMMON_NS, "value", "c"));
            DOMUtil.setXsiType(objectDelta, new QName(Constants.TYPES_NS, "ObjectDeltaType", "t"));
            Element itemDelta = DOMUtil.createSubElement(objectDelta, new QName(Constants.TYPES_NS, "itemDelta", "t"));

            DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "modificationType", "t")).setTextContent("add");
            DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "path", "t")).setTextContent("TODO (e.g. displayName)");
            Element value = DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "value", "t"));
            DOMUtil.setXsiType(value, DOMUtil.XSD_STRING);
            value.setTextContent("TODO");
        } else if (action == Action.EXECUTE_SCRIPT) {
            Element scriptParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
            DOMUtil.createSubElement(scriptParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("script");
            Element script = DOMUtil.createSubElement(scriptParam, new QName(Constants.COMMON_NS, "value", "c"));
            DOMUtil.setXsiType(script, new QName(Constants.COMMON_NS, "ScriptExpressionEvaluatorType", "c"));
            DOMUtil.createSubElement(script, new QName(Constants.COMMON_NS, "code", "c")).setTextContent("\n                    log.info('{}', input.asPrismObject().debugDump())\n                ");
            DOMUtil.createComment(actionE, " <s:parameter><s:name>outputItem</s:name><c:value xmlns:c='http://midpoint.evolveum.com/xml/ns/public/common/common-3'>UserType</c:value></s:parameter> ");
        } else if (action == Action.NOTIFY) {
            Element subtypeParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
            DOMUtil.createSubElement(subtypeParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("subtype");
            DOMUtil.createSubElement(subtypeParam, new QName(Constants.COMMON_NS, "value", "c")).setTextContent("...TODO...");
            Element handlerParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
            DOMUtil.createSubElement(handlerParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("handler");
            Element handlerValue = DOMUtil.createSubElement(handlerParam, new QName(Constants.COMMON_NS, "value", "c"));
            DOMUtil.setXsiType(handlerValue, new QName(Constants.COMMON_NS, "EventHandlerType", "c"));
            Element generalNotifier = DOMUtil.createSubElement(handlerValue, new QName(Constants.COMMON_NS, "generalNotifier", "c"));
            Element recipientExpression = DOMUtil.createSubElement(generalNotifier, new QName(Constants.COMMON_NS, "recipientExpression", "c"));
            DOMUtil.createSubElement(recipientExpression, new QName(Constants.COMMON_NS, "value", "c")).setTextContent("TODO@TODO.com");
            Element bodyExpression = DOMUtil.createSubElement(generalNotifier, new QName(Constants.COMMON_NS, "bodyExpression", "c"));
            Element script = DOMUtil.createSubElement(bodyExpression, new QName(Constants.COMMON_NS, "script", "c"));
            DOMUtil.createSubElement(generalNotifier, new QName(Constants.COMMON_NS, "transport", "c")).setTextContent("mail");
            DOMUtil.createSubElement(script, new QName(Constants.COMMON_NS, "language", "c")).setTextContent("http://midpoint.evolveum.com/xml/ns/public/expression/language#velocity");
            DOMUtil.createSubElement(script, new QName(Constants.COMMON_NS, "code", "c")).setTextContent("event.object is '$event.object.name' with OID of $event.object.oid");
            DOMUtil.createComment(actionE, " Other parameters: forWholeInput, status, operation ");
        } else if (action == Action.ASSIGN_TO_THIS) {
            Element deltaParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
            DOMUtil.createSubElement(deltaParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("delta");
            Element objectDelta = DOMUtil.createSubElement(deltaParam, new QName(Constants.COMMON_NS, "value", "c"));
            DOMUtil.setXsiType(objectDelta, new QName(Constants.TYPES_NS, "ObjectDeltaType", "t"));
            Element itemDelta = DOMUtil.createSubElement(objectDelta, new QName(Constants.TYPES_NS, "itemDelta", "t"));

            DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "modificationType", "t")).setTextContent("add");
            DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "path", "t")).setTextContent("assignment");
            Element assignment = DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "value", "t"));
            DOMUtil.setXsiType(assignment, new QName(Constants.COMMON_NS, "AssignmentType"));
            Element targetRef = DOMUtil.createSubElement(assignment, new QName(Constants.COMMON_NS, "targetRef", "c"));
            targetRef.setAttribute("type", "TODO: target type");
            targetRef.setAttribute("oid", "TODO: target OID");
        } else if (action == Action.ASSIGN_THIS) {
            Element deltaParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
            DOMUtil.createSubElement(deltaParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("delta");
            Element objectDelta = DOMUtil.createSubElement(deltaParam, new QName(Constants.COMMON_NS, "value", "c"));
            DOMUtil.setXsiType(objectDelta, new QName(Constants.TYPES_NS, "ObjectDeltaType", "t"));
            Element itemDelta = DOMUtil.createSubElement(objectDelta, new QName(Constants.TYPES_NS, "itemDelta", "t"));
            DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "modificationType", "t")).setTextContent("add");
            DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "path", "t")).setTextContent("assignment");
            for (ObjectType object : objects) {
                Element assignment = DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "value", "t"));
                DOMUtil.setXsiType(assignment, new QName(Constants.COMMON_NS, "AssignmentType"));
                Element targetRef = DOMUtil.createSubElement(assignment, new QName(Constants.COMMON_NS, "targetRef", "c"));
                createRefContent(targetRef, object, options);
            }
        }

    }

    @Override
    public boolean supportsRawOption() {
        return action.supportsRaw;
    }

    @Override
    public boolean supportsDryRunOption() {
        return action.supportsDryRun;
    }

    @Override
    public boolean isExecutable() {
        return action != Action.MODIFY && action != Action.EXECUTE_SCRIPT;
    }

    @Override
    public boolean supportsWrapIntoTask() {
        return true;
    }

    public String generateFromSourceObject(PrismObject object, GeneratorOptions options) {
        Element pipe = DOMUtil.getDocument(new QName(Constants.SCRIPT_NS, "pipeline", "s")).getDocumentElement();
        createSingleSourceSearch(pipe, object);
        if (action == Action.ASSIGN_THIS) {
            throw new IllegalStateException("'Assign this' is not supported here.");
        }
        createAction(pipe, options, null);
        return DOMUtil.serializeDOMToString(pipe);
    }

    @Override
    protected boolean requiresExecutionConfirmation() {
        return action.requiresConfirmation;
    }

    public String getActionDescription() {
        return action.displayName;
    }

}
