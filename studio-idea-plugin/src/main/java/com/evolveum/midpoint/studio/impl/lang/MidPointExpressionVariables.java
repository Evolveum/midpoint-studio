package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.common.LocalizationService;
import com.evolveum.midpoint.model.api.expr.MidpointFunctions;
import com.evolveum.midpoint.model.common.expression.functions.BasicExpressionFunctions;
import com.evolveum.midpoint.model.common.expression.functions.LogExpressionFunctions;
import com.evolveum.midpoint.model.impl.expr.MidpointFunctionsImpl;
import com.evolveum.midpoint.notifications.api.events.Event;
import com.evolveum.midpoint.schema.processor.ResourceObjectTypeDefinition;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum MidPointExpressionVariables {

    INPUT("input", null, null),

    FOCUS("focus", FocusType.class, null), // todo how to handle subclasses

    USER("user", UserType.class),

    PROJECTION("projection", ShadowType.class),

    RESOURCE("resource", ResourceType.class),

    OPERATION("operation", String.class),

    ACTOR("actor", UserType.class),

    CONFIGURATION("configuration", SystemConfigurationType.class),

    ITERATION("iteration", Integer.class),

    ITERATION_TOKEN("iterationToken", String.class),

    LEGAL("legal", Boolean.class),

    ASSIGNED("assigned", Boolean.class),

    ADMINISTRATIVE_STATUS("administrativeStatus", ActivationStatusType.class),

    FOCUS_EXISTS("focusExists", Boolean.class),

    R("associationTargetObjectClassDefinition", ResourceObjectTypeDefinition.class),

    ENTITLEMENT("entitlement", ShadowType.class),

    BASIC("basic", BasicExpressionFunctions.class),

    MIDPOINT("midpoint", MidpointFunctions.class, MidpointFunctionsImpl.class),

    LOG("log", LogExpressionFunctions.class),

    EVENT("event", Event.class),

    REQUESTER("requester", UserType.class),

    REQUESTEE("requestee", ObjectType.class),

    ASSIGNEE("assignee", UserType.class),

    TRANSPORT_NAME("transportName", String.class),

    LOCALIZATION_SERVICE("localizationService",LocalizationService.class);

    private String variable;

    private Class type;

    private Class instanceType;

    MidPointExpressionVariables(String variable, Class type) {
        this(variable, type, type);
    }

    MidPointExpressionVariables(String variable, Class type, Class instanceType) {
        this.variable = variable;
        this.type = type;
        this.instanceType = instanceType;
    }

    public String getVariable() {
        return variable;
    }

    public Class getType() {
        return type;
    }

    public Class getInstanceType() {
        return instanceType;
    }
}
