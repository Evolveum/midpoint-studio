package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.MidPointObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by lazyman on 10/02/2017.
 */
public class UploadTestResource extends UploadExecute {

    public static final String OPERATION_TEST_CONNECTION = "test connection";

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, MidPointObject obj) throws Exception {
        ProcessObjectResult por = super.processObject(evt, client, obj);
        OperationResult uploadResult = por.result();

        if (uploadResult != null && !uploadResult.isSuccess()) {
            printProblem(evt.getProject(), "Skipping test connection for " + obj.getName() + ", there was a problem with upload");
            return por;
        }

        if (!ObjectTypes.RESOURCE.equals(obj.getType())) {
            printProblem(evt.getProject(), "Can't test connection for " + obj.getName() + ", because it's not resource, it's " + obj.getType().getClassDefinition().getName());
            return por;
        }

        OperationResult testConnectionResult = client.testResource(obj.getOid());
        return validateOperationResult(evt, testConnectionResult, OPERATION_TEST_CONNECTION, obj.getName());
    }
}
