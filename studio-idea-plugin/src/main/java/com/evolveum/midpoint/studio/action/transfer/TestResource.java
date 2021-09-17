package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestResource extends BaseObjectsAction {

    public static final String OPERATION_TEST_CONNECTION = "test connection";

    public TestResource() {
        super("Test resources", "Test Action", "test resource");
    }

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, MidPointObject obj) {
        if (!ObjectTypes.RESOURCE.equals(obj.getType())) {
            printProblem(evt.getProject(), "Can't test connection for " + obj.getName()
                    + ", because it's not resource, it's " + obj.getType().getClassDefinition().getName());

            return new ProcessObjectResult(null).problem(true);
        }

        OperationResult testConnectionResult = client.testResource(obj.getOid());
        return validateOperationResult(evt, testConnectionResult, OPERATION_TEST_CONNECTION, obj.getName());
    }
}
