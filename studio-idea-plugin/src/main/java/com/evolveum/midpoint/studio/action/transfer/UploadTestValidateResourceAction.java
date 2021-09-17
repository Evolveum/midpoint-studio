package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.browse.BulkActionGenerator;
import com.evolveum.midpoint.studio.impl.browse.GeneratorOptions;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadTestValidateResourceAction extends UploadTestResource {

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, MidPointObject obj) throws Exception {
        ProcessObjectResult por = super.processObject(evt, client, obj);
        OperationResult testConnectionResult = por.result();

        String name = obj.getName();

        if (testConnectionResult != null && !testConnectionResult.isSuccess()) {
            printProblem(evt.getProject(), "Skipping resource validation for " + name + ", there was a problem with upload/test");
            return por;
        }

        if (!ObjectTypes.RESOURCE.equals(obj.getType())) {
            printProblem(evt.getProject(), "Can't validate resource for " + name + ", because it's " + obj.getType().getClassDefinition().getName());
            return por;
        }

        GeneratorOptions genOptions = new GeneratorOptions();
        BulkActionGenerator gen = new BulkActionGenerator(BulkActionGenerator.Action.VALIDATE);
        String requestString = gen.generateFromSourceObject(obj, genOptions);

        ExecuteScriptResponseType response = client.execute(requestString);
        OperationResultType res = response.getResult();
        OperationResult executionResult = OperationResult.createOperationResult(res);

        return validateOperationResult(evt, executionResult, "validate", obj.getName());
    }
}
