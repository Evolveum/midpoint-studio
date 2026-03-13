package com.evolveum.midpoint.studio.ui.dialog.wizard;

public enum MidpointStepStatus {

    COMPLETE("Complete"),
    IN_PROGRESS("In progress"),
    FAILED("Failed"),
    NONE("None");

    private final String label;

    MidpointStepStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
