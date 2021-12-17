package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.UploadResponse;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.vcsUtil.VcsUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadExecuteTask extends ClientBackgroundableTask<TaskState> {

    public static String TITLE = "Upload/Execute task";

    public static final String NOTIFICATION_KEY = "Upload/Execute task";

    public UploadExecuteTask(AnActionEvent event, Environment environment) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);
        setEnvironment(environment);
    }

    @Override
    public ProcessObjectResult processObject(MidPointObject obj) throws Exception {
        OperationResult result = uploadExecute(client, obj);

        return validateOperationResult("upload", result, obj.getName());
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
            UploadResponse resp = client.uploadRaw(obj, buildUploadOptions(obj), true, VcsUtil.getVirtualFile(obj.getFile()));
            result = resp.getResult();

            if (obj.getOid() == null && resp.getOid() != null) {
                obj.setOid(resp.getOid());
            }
        }

        return result;
    }

    public static List<String> buildUploadOptions(MidPointObject object) {
        List<String> options = new ArrayList<>();
        options.add("isImport");

        ObjectTypes type = object.getType();
        if (type != ObjectTypes.TASK && type != ObjectTypes.SYSTEM_CONFIGURATION) {
            options.add("raw");
        }

        return options;
    }
}
