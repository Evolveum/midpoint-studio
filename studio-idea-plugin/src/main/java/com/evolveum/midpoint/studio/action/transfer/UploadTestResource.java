package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by lazyman on 10/02/2017.
 */
public class UploadTestResource extends UploadExecute {

    public static final String OPERATION_TEST_CONNECTION = "test connection";

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, PrismObject<O> obj) throws Exception {
        ProcessObjectResult por = super.processObject(evt, client, obj);
        OperationResult uploadResult = por.result();

        if (uploadResult != null && !uploadResult.isSuccess()) {
            printProblem(evt.getProject(), "Skipping test connection for " + MidPointUtils.getName(obj) + ", there was a problem with upload");
            return por;
        }

        if (!ResourceType.class.equals(obj.getCompileTimeClass())) {
            printProblem(evt.getProject(), "Can't test connection for " + MidPointUtils.getName(obj) + ", because it's not resource, it's " + obj.getCompileTimeClass().getName());
            return por;
        }

        OperationResult testConnectionResult = client.testResource(obj.getOid());
        return validateOperationResult(evt, testConnectionResult, OPERATION_TEST_CONNECTION, MidPointUtils.getName(obj));
    }
}
