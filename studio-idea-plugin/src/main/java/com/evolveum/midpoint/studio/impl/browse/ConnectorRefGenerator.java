package com.evolveum.midpoint.studio.impl.browse;


import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public class ConnectorRefGenerator extends RefGenerator {

    public ConnectorRefGenerator() {
        super("connectorRef", ObjectTypes.CONNECTOR);
    }

    @Override
    protected String getSymbolicRefItemValue(ObjectType object) {
        // todo fix
//        return object.getSubtypes().size() == 1 ? object.getSubtypes().get(0) : "FILL IN CONNECTORTYPE HERE";
        return "FILL IN CONNECTORTYPE HERE";
    }

    @Override
    protected String getSymbolicRefItemName(ObjectType object) {
        return "connectorType";
    }

}
