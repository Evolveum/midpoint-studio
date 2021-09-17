package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.client.DeleteOptions;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteAction extends BaseObjectsAction {

    public DeleteAction() {
        this(null);
    }

    public DeleteAction(List<Pair<String, ObjectTypes>> oids) {
        super("Deleting objects", "Delete Action", "delete", oids);
    }

    @Override
    public <O extends ObjectType> ProcessObjectResult processObjectOid(AnActionEvent evt, MidPointClient client, ObjectTypes type, String oid) throws Exception {
        client.delete(type.getClassDefinition(), oid, createOptions());

        return new ProcessObjectResult(null);
    }

    public DeleteOptions createOptions() {
        return new DeleteOptions();
    }
}
