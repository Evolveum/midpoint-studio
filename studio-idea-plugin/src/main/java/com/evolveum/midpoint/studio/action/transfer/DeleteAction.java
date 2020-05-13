package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.client.api.DeleteOptions;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteAction extends BaseObjectsAction {

    public DeleteAction() {
        super("Deleting objects", "Delete Action", "delete");
    }

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(MidPointClient client, PrismObject<O> obj) throws Exception {
        client.delete(obj.getCompileTimeClass(), obj.getOid(), createOptions());

        return new ProcessObjectResult(null);
    }

    public DeleteOptions createOptions() {
        return new DeleteOptions();
    }
}
