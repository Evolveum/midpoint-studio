package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Created by lazyman on 10/02/2017.
 */
public class UploadTestResource extends UploadExecute {

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(MidPointClient client, PrismObject<O> obj) throws Exception {
        ProcessObjectResult por = super.processObject(client, obj);
        OperationResult uploadResult = por.result();
        // todo validate uploadResult

        OperationResult testConnectionResult = client.testResource(obj.getOid());
        // todo validate testConnectionResult

        return por;
    }
}
