package com.evolveum.midpoint.studio.impl.performance;

import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyMap;

/**
 * Key for summarizing operations.
 */
public class OperationKey implements Serializable {

    private static final long serialVersionUID = 8007514029387365317L;

    /**
     * Name of the operation, e.g. "com.evolveum.midpoint.model.impl.lens.Clockwork.click".
     */
    @NotNull final String operationName;

    /**
     * Qualifiers describing the operation, e.g. [ "INITIAL.e0p0" ]
     */
    @NotNull final List<String> qualifiers;

    /**
     * Parameters that are part of the key, e.g. { mapping: "M(a9 = null, strong)" }
     */
    @NotNull final Map<String, String> parameters;

    /**
     * Contextual values that are part of the key.
     */
    @NotNull final Map<String, String> context;

    private OperationKey(@NotNull String operationName, @NotNull List<String> qualifiers,
            @NotNull Map<String, String> parameters, @NotNull Map<String, String> context) {
        this.operationName = operationName;
        this.qualifiers = qualifiers;
        this.parameters = parameters;
        this.context = context;
    }

    /**
     * Extracts relevant information from an operation result.
     */
    public static OperationKey create(OperationResultType operationResult) {
        // TODO parameters and context values
        return new OperationKey(operationResult.getOperation(), operationResult.getQualifier(), emptyMap(), emptyMap());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OperationKey)) {
            return false;
        }
        OperationKey that = (OperationKey) o;
        return operationName.equals(that.operationName) &&
                qualifiers.equals(that.qualifiers) &&
                parameters.equals(that.parameters) &&
                context.equals(that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationName, qualifiers, parameters, context);
    }

    @Override
    public String toString() {
        return "OperationKey{" +
                "operationName='" + operationName + '\'' +
                ", qualifiers=" + qualifiers +
                ", parameters=" + parameters +
                ", context=" + context +
                '}';
    }

    public @NotNull String getOperationName() {
        return operationName;
    }

    public @NotNull List<String> getQualifiers() {
        return qualifiers;
    }

    public @NotNull Map<String, String> getParameters() {
        return parameters;
    }

    public @NotNull Map<String, String> getContext() {
        return context;
    }

    public String getFormattedName() {
        StringBuilder sb = new StringBuilder();
        sb.append(operationName);
        qualifiers.forEach(q -> sb.append(" - ").append(q));
        // TODO params/context
        return sb.toString();
    }
}
