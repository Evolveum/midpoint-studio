package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class UploadExecuteStopOnError extends UploadExecute {

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, PrismObject<O> obj) throws Exception {
        try {
            ProcessObjectResult por = super.processObject(evt, client, obj);
            if (por.problem()) {
                por.shouldContinue(false);
            }

            return por;
        } catch (Exception ex) {
            return new ProcessObjectResult(null).problem(true).shouldContinue(false);
        }
    }
}
