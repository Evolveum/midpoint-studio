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
    public <O extends ObjectType> OperationResult processObject(MidPointClient client, PrismObject<O> obj) throws Exception {
        OperationResult result = super.processObject(client, obj);

        // todo stop on error

        return result;
    }
}
