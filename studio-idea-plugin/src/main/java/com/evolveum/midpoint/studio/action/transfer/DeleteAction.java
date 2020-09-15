package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.studio.impl.client.DeleteOptions;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.MidPointObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteAction extends BaseObjectsAction {

    public DeleteAction() {
        super("Deleting objects", "Delete Action", "delete");
    }

    @Override
    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, MidPointObject obj) throws Exception {
        client.delete(obj.getType().getClassDefinition(), obj.getOid(), createOptions());

        return new ProcessObjectResult(null);
    }

    public DeleteOptions createOptions() {
        return new DeleteOptions();
    }
}
