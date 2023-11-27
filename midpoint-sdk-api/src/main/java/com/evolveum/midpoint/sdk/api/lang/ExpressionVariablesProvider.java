package com.evolveum.midpoint.sdk.api.lang;

import java.util.Map;

public interface ExpressionVariablesProvider {

    // todo parameters describing the context of the expression evaluation
    // eg. type of the object being evaluated, type of the expression, place (template, policy rule, assingment target search, ...)
    Map<String, Variable> getVariables();
}
