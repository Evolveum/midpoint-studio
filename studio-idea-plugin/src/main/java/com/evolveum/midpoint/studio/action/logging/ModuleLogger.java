package com.evolveum.midpoint.studio.action.logging;

/**
 * Created by Viliam Repan (lazyman).
 */
public enum ModuleLogger {

    GUI("com.evolveum.midpoint.web", "Gui"),

    MODEL("com.evolveum.midpoint.model", "Model"),

    PROVISIONING("com.evolveum.midpoint.provisioning", "Provisioning"),

    REPOSITORY("com.evolveum.midpoint.repo", "Repository");

    private final String logger;

    private final String label;

    ModuleLogger(String logger, String label) {
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
