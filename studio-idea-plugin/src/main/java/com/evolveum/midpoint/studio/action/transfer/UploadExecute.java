package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.UploadResponse;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.IOException;
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
        OperationResult result = uploadExecute(client, obj);

        return validateOperationResult(evt, result, getOperation(), obj.getName());
    }

    public static OperationResult uploadExecute(MidPointClient client, MidPointObject obj)
            throws AuthenticationException, IOException, SchemaException {

        OperationResult result = null;
        if (obj.isExecutable()) {
            ExecuteScriptResponseType response = client.execute(obj.getContent());

            if (response != null) {
                OperationResultType res = response.getResult();
                result = OperationResult.createOperationResult(res);
            }
        } else {
            UploadResponse resp = client.uploadRaw(obj, buildUploadOptions(obj), true, obj.getFile());
            result = resp.getResult();
        }

        return result;
    }

    public static <O extends ObjectType> List<String> buildUploadOptions(MidPointObject object) {
        List<String> options = new ArrayList<>();
        options.add("isImport");

        ObjectTypes type = object.getType();
        if (type != ObjectTypes.TASK && type != ObjectTypes.SYSTEM_CONFIGURATION) {
            options.add("raw");
        }

        return options;
    }
}
