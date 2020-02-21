package com.evolveum.midpoint.studio.impl.browse;


import com.evolveum.midpoint.schema.constants.ObjectTypes;

public class ConnectorRefGenerator extends RefGenerator {

    public ConnectorRefGenerator() {
        super("connectorRef", ObjectTypes.CONNECTOR);
    }

    @Override
    protected String getSymbolicRefItemValue(MidPointObject object) {
        return object.getSubtypes().size() == 1 ? object.getSubtypes().get(0) : "FILL IN CONNECTORTYPE HERE";
    }

    @Override
    protected String getSymbolicRefItemName(MidPointObject object) {
        return "connectorType";
    }

}
