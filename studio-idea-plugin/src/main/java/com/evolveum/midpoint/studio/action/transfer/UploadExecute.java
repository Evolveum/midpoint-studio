package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.UploadResponse;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadExecute extends BaseObjectsAction {

    public UploadExecute() {
        super("Uploading objects", "Upload Action", "upload");
    }

    @Override
    public <O extends ObjectType> OperationResult processObject(MidPointClient client, PrismObject<O> obj) throws Exception {
        UploadResponse resp = client.upload(obj, buildAddOptions(obj));
        return resp.getResult();
    }

    public <O extends ObjectType> List<String> buildAddOptions(PrismObject<O> object) {
        List<String> options = new ArrayList<>();
        options.add("isImport");

        ObjectTypes type = ObjectTypes.getObjectType(object.getCompileTimeClass());
        if (type != ObjectTypes.TASK && type != ObjectTypes.SYSTEM_CONFIGURATION) {
            options.add("raw");
        }

        return options;
    }
}
