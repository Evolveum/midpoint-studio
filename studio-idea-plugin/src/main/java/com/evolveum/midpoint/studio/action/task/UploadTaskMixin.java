package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.client.AuthenticationException;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.ShowConsoleNotificationAction;
import com.evolveum.midpoint.studio.impl.UploadResponse;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AssignmentHolderType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ModelExecuteOptionsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.*;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface UploadTaskMixin {

    record UploadExecuteResult(OperationResult result, ExecuteScriptResponseType executeScriptResponse) {

    }

    static void showConsoleOutputNotification(
            Project project, Environment environment, Class caller, String notificationKey, MidPointObject object,
            UploadExecuteResult result) {

        if (result == null || result.executeScriptResponse() == null) {
            return;
        }

        String name = object.isExecutable() ? "action" : object.getName();

        ExecuteScriptOutputType output = result.executeScriptResponse().getOutput();

        Set<OperationResultStatusType> itemStatuses = new HashSet<>();
        if (output != null && output.getDataOutput() != null) {
            itemStatuses = output.getDataOutput().getItem().stream()
                    .map(i -> i.getResult())
                    .filter(r -> r != null && r.getStatus() != null)
                    .map(r -> r.getStatus())
                    .collect(Collectors.toSet());
        }

        String consoleOutput = output.getConsoleOutput();

        MidPointService ms = MidPointService.get(project);
        ms.printToConsole(environment, caller, "Raw console output for " + name + ":\n" + consoleOutput + "\n");

        NotificationType type = NotificationType.INFORMATION;
        if (itemStatuses.isEmpty()
                || itemStatuses.stream().anyMatch(s -> s != OperationResultStatusType.SUCCESS)) {
            type = NotificationType.WARNING;
        }

        if (itemStatuses.stream().anyMatch(s -> s == OperationResultStatusType.FATAL_ERROR)) {
            type = NotificationType.ERROR;
        }

        MidPointUtils.publishNotification(
                project,
                notificationKey, "Action output", StringUtils.abbreviate(consoleOutput, 100),
                type,
                new ShowConsoleNotificationAction(project));
    }

    static List<String> buildUploadOptions(MidPointObject object) {
        List<String> options = new ArrayList<>();
        options.add(ModelExecuteOptionsType.F_IS_IMPORT.getLocalPart());

        ObjectTypes type = object.getType();
        if (type != ObjectTypes.TASK && type != ObjectTypes.SYSTEM_CONFIGURATION) {
            options.add(ModelExecuteOptionsType.F_RAW.getLocalPart());
        }

        return options;
    }

    static UploadExecuteResult uploadExecute(MidPointClient client, MidPointObject obj)
            throws AuthenticationException, IOException, SchemaException {

        return uploadExecute(client, obj, buildUploadOptions(obj));
    }

    static UploadExecuteResult uploadExecute(MidPointClient client, MidPointObject obj, List<String> options)
            throws AuthenticationException, IOException, SchemaException {

        OperationResult result = null;
        ExecuteScriptResponseType executeScriptResponse = null;
        if (obj.isExecutable()) {
            executeScriptResponse = client.execute(obj.getContent());

            if (executeScriptResponse != null) {
                OperationResultType res = executeScriptResponse.getResult();
                result = OperationResult.createOperationResult(res);
            }
        } else {
            File file = obj.getFile();
            VirtualFile vFile = file != null ? VcsUtil.getVirtualFile(file) : null;

            MidPointConfiguration settings = MidPointService.get(client.getProject()).getSettings();
            UploadResponse response;
            if (obj.getOid() != null && settings.isUpdateOnUpload()) {
                try {
                    response = client.modify(obj, options, true, vFile);
                } catch (ObjectNotFoundException ex) {
                    response = client.uploadRaw(obj, options, true, vFile);
                }
            } else {
                response = client.uploadRaw(obj, options, true, vFile);
            }
            result = response.getResult();

            if (obj.getOid() == null && response.getOid() != null) {
                obj.setOid(response.getOid());
            }
        }

        return new UploadExecuteResult(result, executeScriptResponse);
    }

    static OperationResult recompute(MidPointClient client, MidPointObject object)
            throws SchemaException, IOException, AuthenticationException {
        if (object.isExecutable()) {
            return null;
        }

        if (object.getType() == null || !AssignmentHolderType.class.isAssignableFrom(object.getType().getClassDefinition())) {
            return null;
        }

        String requestString = buildExecuteScriptRequestBody(client, object);

        ExecuteScriptResponseType response = client.execute(requestString);
        OperationResultType res = response.getResult();
        return OperationResult.createOperationResult(res);
    }

    private static String buildExecuteScriptRequestBody(MidPointClient client, MidPointObject obj)
            throws SchemaException {
        PrismContext ctx = client.getPrismContext();

        ObjectFactory of = new ObjectFactory();

        ExpressionPipelineType pipeline = new ExpressionPipelineType();
        pipeline.setList(null);
        SearchExpressionType search = new SearchExpressionType();
        pipeline.getScriptingExpression().add(of.createSearch(search));

        ObjectFilter filter = ctx.queryFor(obj.getType().getClassDefinition())
                .id(obj.getOid())
                .buildFilter();
        SearchFilterType searchFilter = ctx.getQueryConverter().createSearchFilterType(filter);

        search.setType(obj.getType().getTypeQName());
        search.setSearchFilter(searchFilter);

        ActionExpressionType action = new ActionExpressionType();
        pipeline.getScriptingExpression().add(of.createAction(action));
        action.setType("recompute");

        return client.serialize(of.createPipeline(pipeline));
    }

    static OperationResult testResourceConnection(MidPointClient client, MidPointObject obj) {
        if (obj.isExecutable()) {
            return null;
        }

        if (!ObjectTypes.RESOURCE.equals(obj.getType())) {
            return null;
        }

        return client.testResource(obj.getOid());
    }
}
