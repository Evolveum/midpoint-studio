package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CleanupFileTask extends ObjectsBackgroundableTask<TaskState> {

    public static final String TITLE = "Cleanup File";

    public static final String NOTIFICATION_KEY = "Cleanup File Action";

    private static final Logger LOG = Logger.getInstance(CleanupFileTask.class);

    public static final Map<Class<? extends ObjectType>, List<ItemPath>> CLEANUP_PATHS = new HashMap<>();

    static {
        CLEANUP_PATHS.put(ObjectType.class, Arrays.asList(
                ItemPath.create(ObjectType.F_METADATA),
                ItemPath.create(ObjectType.F_FETCH_RESULT),
                ItemPath.create(ObjectType.F_OPERATION_EXECUTION),
                ItemPath.create(ObjectType.F_DIAGNOSTIC_INFORMATION)
        ));

        CLEANUP_PATHS.put(AssignmentHolderType.class, Arrays.asList(
                ItemPath.create(AssignmentHolderType.F_ITERATION),
                ItemPath.create(AssignmentHolderType.F_ITERATION_TOKEN),
                ItemPath.create(FocusType.F_ASSIGNMENT, AssignmentType.F_METADATA)
        ));

        CLEANUP_PATHS.put(FocusType.class, Arrays.asList(
                ItemPath.create(FocusType.F_ROLE_MEMBERSHIP_REF),
                ItemPath.create(FocusType.F_ACTIVATION, ActivationType.F_EFFECTIVE_STATUS),
                ItemPath.create(FocusType.F_ACTIVATION, ActivationType.F_ENABLE_TIMESTAMP),
                ItemPath.create(FocusType.F_ACTIVATION, ActivationType.F_DISABLE_TIMESTAMP),
                ItemPath.create(FocusType.F_ACTIVATION, ActivationType.F_DISABLE_REASON),
                ItemPath.create(FocusType.F_CREDENTIALS, CredentialsType.F_PASSWORD, PasswordType.F_METADATA)
        ));

        CLEANUP_PATHS.put(TaskType.class, Arrays.asList(
                ItemPath.create(TaskType.F_RESULT),
                ItemPath.create(TaskType.F_RESULT_STATUS),
                ItemPath.create(TaskType.F_OPERATION_STATS),
                ItemPath.create(TaskType.F_COMPLETION_TIMESTAMP),
                ItemPath.create(TaskType.F_LAST_RUN_FINISH_TIMESTAMP),
                ItemPath.create(TaskType.F_LAST_RUN_START_TIMESTAMP),
                ItemPath.create(new QName(SchemaConstants.NS_C, "workState")),  // TaskType.F_WORK_STATE, not available in 4.4 schema
                ItemPath.create(TaskType.F_TASK_IDENTIFIER),
                ItemPath.create(TaskType.F_PROGRESS),
                ItemPath.create(TaskType.F_DIAGNOSTIC_INFORMATION)
        ));
    }

    public CleanupFileTask(@NotNull AnActionEvent event) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject object) throws Exception {
        String newContent = cleanupObject(object);

        MidPointObject newObject = MidPointObject.copy(object);
        newObject.setContent(newContent);

        return new ProcessObjectResult(null)
                .object(newObject);
    }

    @Override
    protected boolean isUpdateObjectAfterProcessing() {
        return true;
    }

    @Override
    public ProcessObjectResult processObjectOid(ObjectTypes type, String oid) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    private String cleanupObject(MidPointObject object) {
        String content = object.getContent();
        if (content == null) {
            return null;
        }

        Document doc = DOMUtil.parseDocument(content);
        Element root = doc.getDocumentElement();

        ObjectTypes type = object.getType();
        if (type == null) {
            return content;
        }

        boolean cleaned = false;
        for (Class<? extends ObjectType> clazz : CLEANUP_PATHS.keySet()) {
            if (!clazz.isAssignableFrom(type.getClassDefinition())) {
                continue;
            }

            List<ItemPath> paths = CLEANUP_PATHS.get(clazz);
            for (ItemPath path : paths) {
                cleaned = cleaned | cleanupObject(root, path);
            }
        }

        if (!cleaned) {
            return content;
        }

        DOMUtil.normalize(root, false);
        return DOMUtil.serializeDOMToString(doc);
    }

    private static boolean cleanupObject(Element obj, ItemPath path) {
        if (path.isEmpty()) {
            obj.getParentNode().removeChild(obj);
            return true;
        }

        boolean cleaned = false;
        List<Element> elements = DOMUtil.getChildElements(obj, path.firstToName());
        for (Element e : elements) {
            cleaned = cleaned | cleanupObject(e, path.rest());
        }

        return cleaned;
    }
}
