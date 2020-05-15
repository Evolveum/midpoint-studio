package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.browse.BulkActionGenerator;
import com.evolveum.midpoint.studio.impl.browse.GeneratorOptions;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadTestValidateResourceAction extends UploadTestResource {

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, PrismObject<O> obj) throws Exception {
        ProcessObjectResult por = super.processObject(evt, client, obj);
        OperationResult testConnectionResult = por.result();

        String name = MidPointUtils.getName(obj);

        if (testConnectionResult != null && !testConnectionResult.isSuccess()) {
            printProblem(evt.getProject(), "Skipping resource validation for " + name + ", there was a problem with upload/test");
            return por;
        }

        if (!ResourceType.class.equals(obj.getCompileTimeClass())) {
            printProblem(evt.getProject(), "Can't validate resource for " + name + ", because it's " + obj.getCompileTimeClass().getName());
            return por;
        }

        GeneratorOptions genOptions = new GeneratorOptions();
        BulkActionGenerator gen = new BulkActionGenerator(BulkActionGenerator.Action.VALIDATE);
        String requestString = gen.generateFromSourceObject(obj, genOptions);

        Object response = client.execute(requestString);
        // todo handler response from validation

        return null;
    }
}
