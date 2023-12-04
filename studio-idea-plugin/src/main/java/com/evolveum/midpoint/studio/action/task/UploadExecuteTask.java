package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadExecuteTask extends ClientBackgroundableTask<TaskState> {

    public static String TITLE = "Upload/Execute task";

    public static final String NOTIFICATION_KEY = TITLE;

    public UploadExecuteTask(AnActionEvent event, Environment environment) {
        this(event, environment, TITLE, NOTIFICATION_KEY);
    }

    protected UploadExecuteTask(AnActionEvent event, Environment environment, String title, String notificationKey) {
        super(event.getProject(), title, notificationKey);

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
            File file = obj.getFile();
            VirtualFile vFile = file != null ? VcsUtil.getVirtualFile(file) : null;

            MidPointConfiguration settings = MidPointService.getInstance(client.getProject()).getSettings();
            UploadResponse response;
            if (obj.getOid() != null && settings.isUpdateOnUpload()) {
                try {
                    response = client.modify(obj, buildUploadOptions(obj), true, vFile);
                } catch (ObjectNotFoundException ex) {
                    response = client.uploadRaw(obj, buildUploadOptions(obj), true, vFile);
                }
            } else {
                response = client.uploadRaw(obj, buildUploadOptions(obj), true, vFile);
            }
            result = response.getResult();

            if (obj.getOid() == null && response.getOid() != null) {
                obj.setOid(response.getOid());
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
