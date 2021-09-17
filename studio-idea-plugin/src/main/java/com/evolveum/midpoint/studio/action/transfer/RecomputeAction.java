package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class RecomputeAction extends BaseObjectsAction {

    public RecomputeAction() {
        this(null);
    }

    public RecomputeAction(List<Pair<String, ObjectTypes>> oids) {
        super("Recomputing objects", "Recompute action", "recompute", oids);
    }

    @Override
    public <O extends ObjectType> ProcessObjectResult processObjectOid(AnActionEvent evt, MidPointClient client, ObjectTypes type, String oid) throws Exception {
        client.recompute(type.getClassDefinition(), oid);

        return new ProcessObjectResult(null);
    }
}
