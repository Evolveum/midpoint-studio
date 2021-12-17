package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.task.UploadExecuteTask;
import com.evolveum.midpoint.studio.action.task.UploadTestValidateResourceTask;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.browse.BulkActionGenerator;
import com.evolveum.midpoint.studio.impl.browse.GeneratorOptions;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadTestValidateResourceAction extends AsyncAction {

    public static final String ACTION_NAME = "Upload/Test/Validate Resource";

    public UploadTestValidateResourceAction() {
        super(ACTION_NAME);
    }

    @Override
    protected UploadExecuteTask createTask(AnActionEvent e, Environment env) {
        return new UploadTestValidateResourceTask(e, env);
    }
}
