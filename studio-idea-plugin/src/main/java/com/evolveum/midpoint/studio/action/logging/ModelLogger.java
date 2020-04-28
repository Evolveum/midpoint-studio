package com.evolveum.midpoint.studio.action.logging;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum ModelLogger {

    CLOCKWORK_SUMMARY("com.evolveum.midpoint.model.impl.lens.Clockwork", "'clockwork summary' (Clockwork=DEBUG)"),

    PROJECTOR_SUMMARY("com.evolveum.midpoint.model.impl.lens.projector.Projector", "'projector summary' (previous + Projector=TRACE)"),

    MAPPING_TRACE("com.evolveum.midpoint.model.common.mapping.Mapping", "'mapping trace' (previous + Mapping=TRACE)"),

    EXPRESSION_TRACE("com.evolveum.midpoint.model.common.expression.Expression", "'expression trace' (previous + Expression=TRACE)"),

    PROJECTOR_TRACE("com.evolveum.midpoint.model.impl.lens.projector", "'projector trace' (previous + projector.*=TRACE)"),

    LENS_TRACE("com.evolveum.midpoint.model.impl.lens", "'lens trace' (previous + lens.*=TRACE)");

    private String logger;

    private String label;

    ModelLogger(String logger, String label) {
        this.logger = logger;
        this.label = label;
    }

    public String getLogger() {
        return logger;
    }

    public String getLabel() {
        return label;
    }
}
