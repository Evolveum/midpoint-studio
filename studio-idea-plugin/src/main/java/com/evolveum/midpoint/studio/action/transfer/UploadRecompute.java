package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ActionExpressionType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ExpressionPipelineType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ObjectFactory;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.SearchExpressionType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadRecompute extends UploadExecute {

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, MidPointObject obj) throws Exception {
        ProcessObjectResult por = super.processObject(evt, client, obj);

        if (obj.isExecutable()) {
            return por;
        }

        OperationResult uploadResult = por.result();

        if (uploadResult != null && !uploadResult.isSuccess()) {
            printProblem(evt.getProject(), "Skipping recomputation for " + obj.getName() + ", there was a problem with upload");

            return por;
        }

        if (!MidPointUtils.isAssignableFrom(ObjectTypes.FOCUS_TYPE, obj.getType())) {
            return por;
        }

        String requestString = buildExecuteScriptRequestBody(client, obj);

        ExecuteScriptResponseType response = client.execute(requestString);
        OperationResultType res = response.getResult();
        OperationResult executionResult = OperationResult.createOperationResult(res);

        return validateOperationResult(evt, executionResult, "recompute", obj.getName());
    }

    private String buildExecuteScriptRequestBody(MidPointClient client, MidPointObject obj) throws SchemaException {
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
}
