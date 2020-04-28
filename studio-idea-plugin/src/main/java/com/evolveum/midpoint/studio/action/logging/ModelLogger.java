package com.evolveum.midpoint.studio.action.logging;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum ModelLogger {

    CLOCKWORK_SUMMARY("'clockwork summary' (Clockwork=DEBUG)"),

    PROJECTOR_SUMMARY("'projector summary' (previous + Projector=TRACE)"),

    MAPPING_TRACE("'mapping trace' (previous + Mapping=TRACE)"),

    EXPRESSION_TRACE("'expression trace' (previous + Expression=TRACE)"),

    PROJECTOR_TRACE("'projector trace' (previous + projector.*=TRACE)"),

    LENS_TRACE("'lens trace' (previous + lens.*=TRACE)");

    private String label;

    ModelLogger(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
