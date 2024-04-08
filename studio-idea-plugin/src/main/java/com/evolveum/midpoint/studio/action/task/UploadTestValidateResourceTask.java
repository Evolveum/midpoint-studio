package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.browse.ActionGenerator;
import com.evolveum.midpoint.studio.impl.browse.GeneratorOptions;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadTestValidateResourceTask extends UploadTestResourceTask {

    public static String TITLE = "Upload/Test/Validate resource task";

    public static final String NOTIFICATION_KEY = TITLE;

    public UploadTestValidateResourceTask(AnActionEvent event, Environment environment) {
        super(event, environment, TITLE, NOTIFICATION_KEY);
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject obj) throws Exception {
        ProcessObjectResult por = super.processObject(obj);

        OperationResult testConnectionResult = por.result();

        String name = obj.getName();

        if (testConnectionResult != null && !testConnectionResult.isSuccess()) {
            printProblem("Skipping resource validation for " + name + ", there was a problem with upload/test");
            return por;
        }

        if (!ObjectTypes.RESOURCE.equals(obj.getType())) {
            printProblem("Can't validate resource for " + name + ", because it's " + obj.getType().getClassDefinition().getName());
            return por;
        }

        GeneratorOptions genOptions = new GeneratorOptions();
        ActionGenerator gen = new ActionGenerator(ActionGenerator.Action.VALIDATE);
        String requestString = gen.generateFromSourceObject(obj, genOptions, getProject());

        ExecuteScriptResponseType response = client.execute(requestString);
        OperationResultType res = response.getResult();
        OperationResult executionResult = OperationResult.createOperationResult(res);

        return validateOperationResult("validate", executionResult, obj.getName());
    }
}
