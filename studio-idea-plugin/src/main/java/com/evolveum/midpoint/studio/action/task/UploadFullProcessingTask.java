package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ModelExecuteOptionsType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

public class UploadFullProcessingTask extends ClientBackgroundableTask<TaskState> {

    public static String TITLE = "Upload (Full Processing)";

    public static final String NOTIFICATION_KEY = TITLE;

    private static final String OPERATION_UPLOAD = "upload";
    private static final String OPERATION_RECOMPUTE = "recompute";
    private static final String OPERATION_TEST_CONNECTION = "test connection";

    public UploadFullProcessingTask(AnActionEvent event, Environment environment) {
        this(event, environment, TITLE, NOTIFICATION_KEY);
    }

    protected UploadFullProcessingTask(
            AnActionEvent event, Environment environment, String title, String notificationKey) {

        super(event.getProject(), title, notificationKey);

        setEvent(event);
        setEnvironment(environment);
    }

    @Override
    protected ProcessObjectResult processObject(MidPointObject object) throws Exception {
        OperationResult result = UploadTaskMixin.uploadExecute(client, object, buildUploadOptions(getProject(), object));

        ProcessObjectResult por = validateOperationResult(OPERATION_UPLOAD, result, object.getName());

        if (object.isExecutable()) {
            return por;
        }

        OperationResult uploadResult = por.result();

        if (uploadResult != null && !uploadResult.isSuccess()) {
            printProblem("Skipping processing for " + object.getName() + ", there was a problem with upload");

            return por;
        }

        OperationResult recomputeResult = UploadTaskMixin.recompute(client, object);
        if (recomputeResult != null) {
            validateOperationResult(OPERATION_RECOMPUTE, recomputeResult, object.getName());
        }

        OperationResult testConnectionResult = UploadTaskMixin.testResourceConnection(client, object);

        return validateOperationResult(OPERATION_TEST_CONNECTION, testConnectionResult, object.getName());
    }

    public static List<String> buildUploadOptions(Project project, MidPointObject object) {
        List<String> options = new ArrayList<>();
        options.add(ModelExecuteOptionsType.F_IS_IMPORT.getLocalPart());

        ObjectTypes type = object.getType();
        if (type != ObjectTypes.TASK && type != ObjectTypes.SYSTEM_CONFIGURATION) {
            options.add(ModelExecuteOptionsType.F_RAW.getLocalPart());
        }

        MidPointConfiguration settings = MidPointService.get(project).getSettings();
        if (settings.isUpdateOnUpload()) {
            options.add(ModelExecuteOptionsType.F_REEVALUATE_SEARCH_FILTERS.getLocalPart());
        }

        return options;
    }
}