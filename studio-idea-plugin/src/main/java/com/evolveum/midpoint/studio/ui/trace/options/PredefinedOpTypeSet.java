package com.evolveum.midpoint.studio.ui.trace.options;

import com.evolveum.midpoint.schema.traces.OpType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public enum PredefinedOpTypeSet {

    ALL("All", Arrays.asList(OpType.values())),
    FUNCTIONAL_OVERVIEW("Functional overview", Arrays.asList(
            OpType.PROJECTOR_INBOUND, OpType.PROJECTOR_TEMPLATE_BEFORE_ASSIGNMENTS, OpType.PROJECTOR_TEMPLATE_AFTER_ASSIGNMENTS,

            OpType.RESOURCE_OBJECT_CONSTRUCTION_EVALUATION, OpType.MAPPING_EVALUATION, OpType.MAPPING_TIME_VALIDITY_EVALUATION,
            OpType.ITEM_CONSOLIDATION,

            OpType.PROJECTOR_ASSIGNMENTS, OpType.ASSIGNMENT_EVALUATION,
            OpType.PROJECTOR_PROJECTION, OpType.PROJECTION_ACTIVATION,

            OpType.PROJECTOR_FOCUS_POLICY_RULES, OpType.POLICY_RULE_EVALUATION,

            OpType.FOCUS_REPOSITORY_LOAD, OpType.FULL_PROJECTION_LOAD,

            OpType.FOCUS_CHANGE_EXECUTION, OpType.PROJECTION_CHANGE_EXECUTION)),

    FUNCTIONAL_OVERVIEW_METADATA("Functional overview (metadata)", Arrays.asList(
            OpType.PROJECTOR_INBOUND, OpType.PROJECTOR_TEMPLATE_BEFORE_ASSIGNMENTS, OpType.PROJECTOR_TEMPLATE_AFTER_ASSIGNMENTS,

            OpType.RESOURCE_OBJECT_CONSTRUCTION_EVALUATION, OpType.MAPPING_EVALUATION, OpType.MAPPING_TIME_VALIDITY_EVALUATION,

            OpType.TRANSFORMATION_EXPRESSION_EVALUATION, OpType.VALUE_TUPLE_TRANSFORMATION,
            OpType.VALUE_METADATA_COMPUTATION, OpType.SCRIPT_EXECUTION,

            OpType.ITEM_CONSOLIDATION,

            OpType.PROJECTOR_ASSIGNMENTS, OpType.ASSIGNMENT_EVALUATION,
            OpType.PROJECTOR_PROJECTION, OpType.PROJECTION_ACTIVATION,

            OpType.PROJECTOR_FOCUS_POLICY_RULES, OpType.POLICY_RULE_EVALUATION,

            OpType.FOCUS_REPOSITORY_LOAD, OpType.FULL_PROJECTION_LOAD,

            OpType.FOCUS_CHANGE_EXECUTION, OpType.PROJECTION_CHANGE_EXECUTION)),

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
