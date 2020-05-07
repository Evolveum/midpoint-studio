package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.browse.BulkActionGenerator;
import com.evolveum.midpoint.studio.impl.browse.GeneratorOptions;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadTestValidateResourceAction extends UploadTestResource {

    @Override
    public <O extends ObjectType> OperationResult processObject(MidPointClient client, PrismObject<O> obj) throws Exception {
        OperationResult testConnectionResult = super.processObject(client, obj);

        // todo validate testConnectionResult

        GeneratorOptions genOptions = new GeneratorOptions();
        BulkActionGenerator gen = new BulkActionGenerator(BulkActionGenerator.Action.VALIDATE);
        String requestString = gen.generateFromSourceObject(obj, genOptions);

        Object response = client.execute(requestString);
        // todo handler response from validation

        return null;
    }
}
