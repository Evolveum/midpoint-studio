package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public enum PredefinedOpTypeSet {

    ALL("All", Arrays.asList(OpType.values())),
    FUNCTIONAL_OVERVIEW("Functional overview", Arrays.asList(OpType.PROJECTOR_INBOUND, OpType.PROJECTOR_TEMPLATE_BEFORE_ASSIGNMENTS,
            OpType.PROJECTOR_TEMPLATE_AFTER_ASSIGNMENTS, OpType.PROJECTOR_ASSIGNMENTS, OpType.ASSIGNMENT_EVALUATION,
            OpType.PROJECTION_ACTIVATION,
            OpType.MAPPING_EVALUATION, OpType.TRANSFORMATION_EXPRESSION_EVALUATION, OpType.VALUE_TUPLE_TRANSFORMATION,
            OpType.MAPPING_TIME_VALIDITY_EVALUATION,
            OpType.VALUE_METADATA_COMPUTATION, OpType.ITEM_CONSOLIDATION, OpType.SCRIPT_EXECUTION, OpType.FOCUS_CHANGE_EXECUTION,
            OpType.PROJECTION_CHANGE_EXECUTION)),
    ASSIGNMENTS_EVALUATION_OVERVIEW("Assignments evaluation overview", Arrays.asList(OpType.PROJECTOR_ASSIGNMENTS, OpType.ASSIGNMENT_EVALUATION,
            OpType.ASSIGNMENT_SEGMENT_EVALUATION)),
    NONE("None", Collections.emptySet());

    private final String name;
    private final Collection<OpType> types;

    PredefinedOpTypeSet(String name, Collection<OpType> types) {
        this.name = name;
        this.types = types;
    }

    @Override
    public String toString() {
        return name;
    }

    public Collection<OpType> getTypes() {
        return types;
    }

    public boolean contains(OpType type) {
        return types.contains(type);
    }
}
