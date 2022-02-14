package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestResourceTask extends ClientBackgroundableTask<TaskState> {

    public static String TITLE = "Test resource task";

    public static final String NOTIFICATION_KEY = "Test resource task";

    public TestResourceTask(AnActionEvent event, Environment environment) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);
        setEnvironment(environment);
    }

    @Override
    protected boolean shouldSkipObjectProcessing(MidPointObject object) {
        return !ObjectTypes.RESOURCE.equals(object.getType());
    }

    @Override
    public ProcessObjectResult processObjectOid(ObjectTypes type, String oid) throws Exception {
        OperationResult result = client.recompute(type.getClassDefinition(), oid);

        return new ProcessObjectResult(result);
    }
}
