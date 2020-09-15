package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.MidPointObject;
import com.evolveum.midpoint.studio.impl.UploadResponse;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;

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
    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, MidPointObject obj) throws Exception {
        UploadResponse resp = client.uploadRaw(obj, buildAddOptions(obj));
        OperationResult result = resp.getResult();

        return validateOperationResult(evt, result, getOperation(), obj.getName());
    }

    public <O extends ObjectType> List<String> buildAddOptions(MidPointObject object) {
        List<String> options = new ArrayList<>();
        options.add("isImport");

        ObjectTypes type = object.getType();
        if (type != ObjectTypes.TASK && type != ObjectTypes.SYSTEM_CONFIGURATION) {
            options.add("raw");
        }

        return options;
    }
}
