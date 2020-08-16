package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.browse.BulkActionGenerator;
import com.evolveum.midpoint.studio.impl.browse.GeneratorOptions;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadRecompute extends UploadExecute {

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, PrismObject<O> obj) throws Exception {
        ProcessObjectResult por = super.processObject(evt, client, obj);
        OperationResult uploadResult = por.result();

        if (uploadResult != null && !uploadResult.isSuccess()) {
            printProblem(evt.getProject(), "Skippint recomputation for " + MidPointUtils.getName(obj) + ", there was a problem with upload");

            return por;
        }

        if (!MidPointUtils.isAssignableFrom(ObjectTypes.FOCUS_TYPE,
                ObjectTypes.getObjectType(obj.getCompileTimeClass()))) {

            return por;
        }

        GeneratorOptions genOptions = new GeneratorOptions();
        BulkActionGenerator gen = new BulkActionGenerator(BulkActionGenerator.Action.RECOMPUTE);
        String requestString = gen.generateFromSourceObject(obj, genOptions);

        ExecuteScriptResponseType response = client.execute(requestString);
        OperationResultType res = response.getResult();
        OperationResult executionResult = OperationResult.createOperationResult(res);

        return validateOperationResult(evt, executionResult, "recompute", MidPointUtils.getName(obj));
    }
}
