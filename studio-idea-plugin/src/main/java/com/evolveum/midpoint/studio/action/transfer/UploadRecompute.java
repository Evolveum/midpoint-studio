package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
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
        return super.processObject(client, obj);

        // todo execute bulk action "RECOMPUTE"
    }
}
