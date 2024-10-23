package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;

public enum DiffStrategy {

    LITERAL("Literal", ParameterizedEquivalenceStrategy.LITERAL),

    DATA("Data", ParameterizedEquivalenceStrategy.DATA),

    IGNORE_METADATA("Ignore Metadata", ParameterizedEquivalenceStrategy.IGNORE_METADATA),

    DEFAULT("Default Compare", ParameterizedEquivalenceStrategy.FOR_DELTA_ADD_APPLICATION),

    REAL_VALUE("Real Value", ParameterizedEquivalenceStrategy.REAL_VALUE),

    REAL_VALUE_CONSIDER_DIFFERENT_IDS("Real Value (consider different IDs)", EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS),

    NATURAL_KEYS("Natural Keys", EquivalenceStrategy.REAL_VALUE_CONSIDER_DIFFERENT_IDS_NATURAL_KEYS);

    private final String label;

    private final ParameterizedEquivalenceStrategy strategy;

    DiffStrategy(String label, ParameterizedEquivalenceStrategy strategy) {
        this.label = label;
        this.strategy = strategy;
    }

    public String getLabel() {
        return label;
    }

    public ParameterizedEquivalenceStrategy getStrategy() {
        return strategy;
    }
}
