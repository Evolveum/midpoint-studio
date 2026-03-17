package com.evolveum.midpoint.studio.ui.dialog.wizard;

public enum WizardStepStatus {

    COMPLETE("Complete"),
    IN_PROGRESS("In progress"),
    PENDING("Pending"),
    FAILED("Failed"),
    NONE("None");

    private final String label;

    WizardStepStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
