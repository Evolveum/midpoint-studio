package com.evolveum.midpoint.sdk.impl.lang;

import com.evolveum.midpoint.sdk.api.lang.ExpressionVariablesProvider;
import com.evolveum.midpoint.sdk.api.lang.Variable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpressionVariableProviderImpl implements ExpressionVariablesProvider {

    @Override
    public Map<String, Variable> getVariables() {
        return Arrays.stream(ExpressionVariable.values())
                .map(v -> new Variable(v.getVariable(), v.getType(), v.getInstanceType()))
                .collect(Collectors.toMap(Variable::name, v -> v));
    }
}
