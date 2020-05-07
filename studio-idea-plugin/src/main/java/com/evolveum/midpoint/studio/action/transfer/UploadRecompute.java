package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.browse.BulkActionGenerator;
import com.evolveum.midpoint.studio.impl.browse.GeneratorOptions;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadRecompute extends UploadExecute {

    @Override
    public <O extends ObjectType> OperationResult processObject(MidPointClient client, PrismObject<O> obj) throws Exception {
        OperationResult uploadResult = super.processObject(client, obj);

        if (!MidPointUtils.isAssignableFrom(ObjectTypes.FOCUS_TYPE,
                ObjectTypes.getObjectType(obj.getCompileTimeClass()))) {

            return uploadResult;
        }

        GeneratorOptions genOptions = new GeneratorOptions();
        BulkActionGenerator gen = new BulkActionGenerator(BulkActionGenerator.Action.RECOMPUTE);
        String requestString = gen.generateFromSourceObject(obj, genOptions);

        Object response = client.execute(requestString);

        // todo fix result and handle response
        return uploadResult;
    }
}
