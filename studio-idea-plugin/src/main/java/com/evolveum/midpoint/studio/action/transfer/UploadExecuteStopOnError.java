package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadExecuteStopOnError extends UploadExecute {

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(MidPointClient client, PrismObject<O> obj) throws Exception {
        try {
            ProcessObjectResult por = super.processObject(client, obj);
            OperationResult result = por.result();
            // todo validate error

            return por;
        } catch (Exception ex) {
            // todo
            return new ProcessObjectResult(null).problem(true).shouldContinue(false);
        }
    }
}
